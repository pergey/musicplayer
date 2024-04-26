import json
import os
import re
import aiofiles
import aiohttp
from pathlib import Path

import ffmpeg
from redis.asyncio import Redis

from server.config import STATIC_DIR
from server.server_types import Song
from server.utils.time import pretty_time

headers = {
    "Origin": "https://music.youtube.com",
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:88.0) Gecko/20100101 Firefox/88.0",
}
search_params = {
    "params": "EgWKAQIIAWoMEA4QChADEAQQCRAF",
    "context": {
        "client": {
            "clientName": "WEB_REMIX",
            "clientVersion": "1.20240420.01.00",
            "hl": "en"
        },
        "user": {}
    },
}
params_for_extract_info = {
    "context": {
        "client": {
            "clientName": "ANDROID",
            "clientVersion": "19.09.37",
            "hl": "en",
        }
    },
    "params": "CgIIAQ==",
    "playbackContext": {
        "contentPlaybackContext": {
            "autoCaptionsDefaultOn": False,
            "html5Preference": "HTML5_PREF_WANTS"
        }
    },
    "contentCheckOk": True,
    "racyCheckOk": True
}

SEARCH_URL = "https://music.youtube.com/youtubei/v1/search?alt=json"
EXTRACT_URL = "https://www.youtube.com/youtubei/v1/player"


def get_flex_column_item(item, index) -> dict | None:
    if (
            len(item["flexColumns"]) <= index
            or "text" not in item["flexColumns"][index]["musicResponsiveListItemFlexColumnRenderer"]
            or "runs" not in item["flexColumns"][index]["musicResponsiveListItemFlexColumnRenderer"]["text"]
    ):
        return None

    return item["flexColumns"][index]["musicResponsiveListItemFlexColumnRenderer"]


def parse_song_details(runs: list[dict]) -> dict:
    parsed = {"artists": []}

    for i, run in enumerate(runs):
        if i % 2:
            continue
        text = run["text"]

        if "navigationEndpoint" in run:
            browse_id = run["navigationEndpoint"]["browseEndpoint"]["browseId"]

            if browse_id and (browse_id.startswith("MPRE") or "release_detail" in browse_id):
                parsed["album"] = text
            else:
                parsed["artists"].append(text)
        else:
            if re.match(r"^(\d+:)*\d+:\d+$", text):
                parsed["duration"] = text
            elif re.match(r"^\d([^ ])* [^ ]*$", text) and i > 0 or re.match(r"^\d{4}$", text):
                pass
            else:
                parsed["artists"].append(text)

    return parsed


def get_audio_info(audio_format: dict) -> dict:
    return {
        "url": audio_format["url"],
        "bitrate": audio_format["bitrate"],
        "size": audio_format["contentLength"],
        "sampleRate": audio_format["audioSampleRate"],
        "mimeType": audio_format["mimeType"]
    }


def get_audio_formats(formats: list[dict]) -> list[dict]:
    return [get_audio_info(any_format) for any_format in formats if any_format["mimeType"].startswith("audio/webm")]


async def get_song(song_id: str, redis: Redis) -> Song | None:
    try:
        raw_song = await redis.get(f"player:songs:{song_id}")

        try:
            raw_song = json.loads(raw_song)
            song = Song(**raw_song)
        except TypeError:
            song = None

        if song is None or (song and song.audio is None):
            params_for_extract_info["videoId"] = song_id

            async with aiohttp.ClientSession() as session:
                async with session.post(EXTRACT_URL, headers=headers, json=params_for_extract_info) as resp:
                    song_data = await resp.json()

            audio_formats = get_audio_formats(song_data["streamingData"]["adaptiveFormats"])
            filepath = f"{STATIC_DIR}/{song_id}.mp3"
            await download_song(song_id, audio_formats, filepath)
            details = song_data["videoDetails"]

            if song is None:
                song = Song(id=song_id, title=details["title"], duration=pretty_time(int(details["lengthSeconds"])),
                            thumbnails=details["thumbnail"]["thumbnails"], audio=f"/{filepath}")
            else:
                song.audio = f"/{filepath}"

            await redis.set(f"player:songs:{song_id}", song.model_dump_json())

        return song
    except KeyError:
        return None


async def search_songs(query: str) -> list[Song]:
    try:
        songs = []
        search_params["query"] = query
        async with aiohttp.ClientSession() as session:
            async with session.post(SEARCH_URL, headers=headers, json=search_params) as resp:
                response = await resp.json()
        raw_songs = \
            response["contents"]["tabbedSearchResultsRenderer"]["tabs"][0]["tabRenderer"]["content"][
                "sectionListRenderer"][
                "contents"][0]["musicShelfRenderer"]["contents"]
        del response

        for raw_song in raw_songs:
            raw_song = raw_song["musicResponsiveListItemRenderer"]
            song_id = raw_song["playlistItemData"]["videoId"]
            thumbnails = raw_song["thumbnail"]["musicThumbnailRenderer"]["thumbnail"]["thumbnails"]

            title_item = get_flex_column_item(raw_song, 0)
            params_item = get_flex_column_item(raw_song, 1)

            if title_item is None or params_item is None:
                continue

            title = title_item["text"]["runs"][0]["text"]
            parsed_details = parse_song_details(params_item["text"]["runs"])

            song = Song(id=song_id, title=title, thumbnails=thumbnails, isExplicit=False, **parsed_details)

            try:
                if "badges" in raw_song:
                    if raw_song["badges"][0]["musicInlineBadgeRenderer"]["icon"]["iconType"] == "MUSIC_EXPLICIT_BADGE":
                        song.isExplicit = True
            except (KeyError, IndexError):
                pass

            songs.append(song)
        return songs
    except KeyError:
        return []


async def download_song(song_id: str, songs: list[dict], filepath: str):
    if not Path(filepath).exists():
        song_info = songs[-1]
        headers["Range"] = f"bytes=0-{song_info['size']}"

        async with aiohttp.ClientSession() as session:
            async with session.post(song_info["url"], headers=headers) as resp:
                temp_file = f"temp/{song_id}.mp4"
                async with aiofiles.open(temp_file, "wb") as f:
                    await f.write(await resp.read())

        ffmpeg.input(temp_file, loglevel="fatal").output(filepath).run()
        os.remove(temp_file)


# async def main():
#     query = input("Введіть назву пісні: ")
#     print("Шукаю пісні... Зачекай трішки будь-ласка!")
#     start_search = time.perf_counter()
#     songs = await search_songs(query)
#     print(songs)
#     print(f"Пошук тривав - {timedelta(seconds=time.perf_counter() - start_search)}\n")
#
#     print("Результат пошуку:")
#     for i, song in enumerate(songs, start=1):
#         artists = " & ".join(song["artists"])
#
#         album = f"\n\tАльбом: {song['album']}" if song["album"] != song['title'] else ""
#         print(f"{i}.\tНазва: {song['title']}{album}\n\tВиконавці: {artists}\n\tТривалість: {song['duration']}\n")
#
#     try:
#         song_index = int(input("Введіть номер пісні для завантаження: "))
#         song = songs[song_index - 1]
#     except (ValueError, TypeError):
#         song = songs[0]
#
#     print(f"Завантажую пісню {song['title']} - {' & '.join(song['artists'])}")
#
#     try:
#         start_download = time.perf_counter()
#         filepath = await download_song(song["songId"])
#         print(f"Успішне завантаження! Шлях до файлу - {filepath}, "
#               f"завантажено за {timedelta(seconds=time.perf_counter() - start_download)}")
#     except:
#         print("Виникла помилка під час завантаження :(")

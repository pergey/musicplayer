import json

from fastapi import APIRouter, Depends
from fastapi.responses import JSONResponse, Response
from redis.asyncio import Redis

from server.server_types import Song
from server.utils.music import search_songs, get_song
from server.dependencies import get_redis

router = APIRouter(
    prefix="/songs",
    responses={404: {"description": "Not found"}}
)


@router.get("/get/{song_id}", response_model=None)
async def get(song_id: str, redis: Redis = Depends(get_redis)) -> Song | Response:
    song = await get_song(song_id, redis)
    if song:
        return song
    else:
        return JSONResponse({"error": "Song not found"}, status_code=404)


@router.get("/search")
async def search(q: str, redis: Redis = Depends(get_redis)) -> list[Song]:
    songs = await search_songs(q)

    for song in songs:
        cached_song = await redis.get(f"player:songs:{song.id}")
        if not cached_song:
            await redis.set(f"player:songs:{song.id}", song.model_dump_json())
        else:
            cached_song = Song(**json.loads(cached_song))
            if not cached_song.album:
                song.audio = cached_song.audio
                await redis.set(f"player:songs:{song.id}", song.model_dump_json())

    return songs

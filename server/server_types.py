from pydantic import BaseModel


class Song(BaseModel):
    id: str
    title: str
    duration: str
    thumbnails: list[dict[str, str | int]]
    album: str | None = None
    artists: list[str] | None = None
    isExplicit: bool | None = None
    audio: str | None = None

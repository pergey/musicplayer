from fastapi import APIRouter

from server.routers.api.v1 import songs
from server.routers.api.v1 import users

router = APIRouter(
    prefix="/v1",
    responses={404: {"description": "Not found"}}
)

router.include_router(songs.router)
router.include_router(users.router)

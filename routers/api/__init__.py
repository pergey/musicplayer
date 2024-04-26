from fastapi import APIRouter
from routers.api.v1 import router

api_router = APIRouter(
    prefix="/api",
    responses={404: {"description": "Not found"}}
)

api_router.include_router(router)

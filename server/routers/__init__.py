from fastapi import APIRouter

from server.routers.api import api_router

router = APIRouter(prefix="/player")
router.include_router(api_router)
__all__ = ["router"]

from fastapi import APIRouter

from routers.api import api_router

router = APIRouter(prefix="/player")
router.include_router(api_router)
__all__ = ["router"]

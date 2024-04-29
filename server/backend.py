from fastapi import FastAPI, Depends
from fastapi.responses import JSONResponse
from fastapi.staticfiles import StaticFiles
from tortoise.contrib.fastapi import register_tortoise

from config import DATABASE_URL, STATIC_DIR
from dependencies import get_redis
from routers import router


app = FastAPI(dependencies=[Depends(get_redis)])
app.mount("static", StaticFiles(directory=STATIC_DIR), name="static")


@app.get("/")
async def root():
    return JSONResponse({"status": "ok"})


app.include_router(router)

register_tortoise(
    app,
    db_url=DATABASE_URL,
    modules={"models": ["models"]},
    generate_schemas=False,
    add_exception_handlers=True,
)

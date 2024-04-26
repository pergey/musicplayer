from tortoise import fields
from tortoise.models import Model


class Users(Model):
    username = fields.CharField(max_length=25)
    email = fields.CharField(max_length=255)
    password_hash = fields.CharField(max_length=255)


class AuthTokens(Model):
    user_id = fields.IntField()
    token = fields.CharField(max_length=255)
    expires = fields.DatetimeField()


class Favorites(Model):
    user_id = fields.IntField()
    song_id = fields.CharField(max_length=255)

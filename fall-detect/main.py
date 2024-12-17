import os
import random
import string
from typing import Union, List
from fastapi import FastAPI, Query, HTTPException
from pydantic import BaseModel
import socket
from urllib.request import urlretrieve
from tools import image_to_pose, fall_detection

app = FastAPI()

socket.setdefaulttimeout(10)

class PoseDetectionResponse(BaseModel):
    status: int
    error: Union[str, None]
    data: Union[List[List[float]], None]
    fall: bool


@app.get("/predict/")
async def predict(image_url: str = Query(...)):
    try:
        filename = ''.join(random.choice(string.ascii_letters + string.digits) for _ in range(10)) + '.jpg'
        local_filename = f"pic/{filename}"
        
        # urlretrieve(image_url, local_filename)
        # local_filename = "C:/Users/WANGZHENHAO/Desktop/photo-1531427186611-ecfd6d936c79.jpg"
        local_filename = image_url
        print(local_filename)
        poses = image_to_pose(local_filename)
        print(len(poses[0]))
        print(poses)
        result = fall_detection(poses)[0]
        
        return PoseDetectionResponse(status=200, error=None, data=poses,fall=result)
    except socket.timeout:
        return PoseDetectionResponse(status=500, error="get image_url pic time out", data=None,fall=None)
    except Exception as e:
        return PoseDetectionResponse(status=500, error=str(e), data=None,fall=None)
    # finally:
    #     if os.path.exists(local_filename):
    #         os.remove(local_filename)   
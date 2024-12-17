# fall-detect

#### 使用说明

1. Clone this repository into your drive.
2. Download the [YOLOv7-POSE](https://github.com/WongKinYiu/yolov7/releases/download/v0.1/yolov7-w6-pose.pt) model into fall-detect directory.
3. Install all the requirements with `pip install -r requirements.txt`
4. Run `uvicorn main:app`

http://localhost:8000/predict/?image_url=https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?ixlib=rb-4.0.3

http://localhost:8000/predict/?image_url=https://images.unsplash.com/photo-1531427186611-ecfd6d936c79?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8Nnx8cGVyc29uJTIwZnVsbC1pbWFnZXxlbnwwfHwwfHw%3D&auto=format&fit=crop&w=1000&q=80

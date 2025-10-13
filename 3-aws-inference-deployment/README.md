# AWS Inference Deployment

## Overview

This project demonstrates deployment of a large language model (LLM) using **AWS services.** It includes a custom-trained LLM, but since it isn’t trained on enough data to produce meaningful results, the **Command R LLM** via **Amazon Bedrock** is used for convenience. The setup showcases a full serverless workflow, including an **Akka HTTP server on EC2, AWS API Gateway, and AWS Lambda.**

## Components
- **Akka HTTP Server on EC2:** A Scala-based server running on AWS EC2, using Akka HTTP to handle incoming requests and send them to the API Gateway.
- **AWS API Gateway:** A managed service that receives HTTP requests, invokes the Lambda function, and passes the response back to the Akka HTTP server.
- **AWS Lambda:** A serverless compute function that processes the request and communicates with Amazon Bedrock's Command R LLM.
- **Amazon Bedrock:** A suite of machine learning models from AWS, with the Command R LLM providing capabilities for large-scale text generation and processing.
- **Command R LLM:** A pre-trained model hosted on Amazon Bedrock, capable of performing tasks like text generation, summarization, and more, based on the input request.

## Technologies Used
- **Akka HTTP:** A toolkit and runtime for building HTTP-based services on the JVM, used in the backend server.
- **Amazon EC2:** AWS's cloud compute service, used to host the Akka HTTP server.
- **AWS API Gateway:** A service to create, deploy, and manage APIs, used for routing requests to the Lambda function.
- **AWS Lambda:** A serverless compute service to run the application code in response to HTTP requests via API Gateway.
- **Amazon Bedrock:** AWS's platform for hosting and using pretrained models like the Command R LLM.
- **Command R LLM:** A state-of-the-art large language model hosted on Amazon Bedrock, used for text processing tasks.

## Flow Description
### **1. User Request**
   A user sends a request (e.g., asking for text generation, summarization, etc.) to the Akka HTTP server on EC2.

### **2. Processing in Akka HTTP Server**
   The Akka server processes the incoming request and forwards it to the API Gateway, where it will be routed to the Lambda function.

### **3. API Gateway to Lambda**
   The API Gateway forwards the request to the AWS Lambda function.

### **4. Lambda and Amazon Bedrock Integration**
   The Lambda function uses Amazon Bedrock's API to interact with the Command R LLM, sending the request for processing.

### **5. Response from Command R LLM**
   The Command R LLM processes the request and sends the result (e.g., generated text) back to the Lambda function.

### **6. Lambda to API Gateway**

   The Lambda function sends the processed result back to the API Gateway.

### **7. API Gateway to Akka HTTP Server**
   The API Gateway forwards the result back to the Akka HTTP server on EC2.

### **8. User Response**
   The Akka HTTP server formats the result and sends it back to the user.



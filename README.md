# Yet Another LLM (YALLM)

## Overview
**YALLM (Yet Another Large Language Model)** is a three-part project that demonstrates the **end-to-end development pipeline** of a large language model — from data preprocessing and model training to deployment on AWS.  
The goal of the project is to explore distributed computing and cloud-based model deployment using **Hadoop**, **Spark**, **DeepLearning4j**, and **AWS services**.

YALLM is structured into three main components:
1. **Data Preprocessing (Hadoop):** Shards raw text data, tokenizes it, and generates word embeddings.  
2. **Model Training (Spark):** Trains a neural network on preprocessed embeddings using sliding window techniques.  
3. **Inference Deployment (AWS):** Deploys the model using an Akka HTTP server, AWS Lambda, API Gateway, and Amazon Bedrock’s Command R LLM for inference.

---

## Repository Structure
```
yallm/
├── hadoop-data-preprocessing/
│  └── README.md
├── spark-model-training/
│  └── README.md
├── aws-model-serving/
│  └── README.md
└── README.md
```
Each directory contains a dedicated README explaining that component in detail.

---

## Quick Links
- [🧱 Hadoop Data Preprocessing](./1-hadoop-data-preprocessing/README.md)  
- [⚡ Spark Model Training](./2-spark-model-training/README.md)  
- [☁️ AWS Model Serving](./3-aws-model-serving/README.md)

---

## Technologies Used
- **Scala**  
- **Apache Hadoop (MapReduce)**  
- **Apache Spark**  
- **DeepLearning4j (DL4J)**  
- **AWS EC2, Lambda, API Gateway, and Bedrock**  
- **Akka HTTP**

---

## High-Level Flow
1. **Preprocessing (Hadoop):** Large text corpus is split, tokenized, and processed to generate word embeddings.  
2. **Training (Spark):** Preprocessed embeddings are used to train a neural network model using sliding window techniques.  
3. **Deployment (AWS):** The trained model (or Command R via Bedrock for convenience) is deployed on AWS for inference.

---

## Purpose
This project was developed to explore the **full pipeline of LLM systems** — including distributed data processing, model training, and scalable inference deployment.  
It serves as a learning-focused demonstration of how large-scale AI systems are structured across multiple platforms.

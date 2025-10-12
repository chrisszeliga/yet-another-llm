# Spark Model Training

## Overview

This project leverages **Apache Spark** and **DeepLearning4j** to train a **large language model (LLM)** using sliding window techniques. 
The main workflow involves reading pre-trained token embeddings, tokenizing a text corpus, and generating input-output pairs based on a sliding window approach. 
These pairs are used to train a neural network model, which is then saved for further use. The architecture includes dense layers optimized with the Adam optimizer, 
and the training process is designed for distributed execution across Spark clusters.

## Project Structure
**1. Main.scala** 
- Entry point for the application.
- Loads configuration, sets up Spark context, and initiates the sliding window processing and model training.

**2. SlidingWindow.scala** 
- Handles the sliding window logic for generating training data.
- Reads token embeddings from a specified file and performs tokenization on the input text.
- Generates **WindowData** objects containing windows of embeddings and their corresponding labels.

**3. Training.scala**
- Contains the logic for training the neural network model.
- Configures the model architecture, including dense layers and the output layer.
- Manages the training process using Spark's distributed capabilities and saves the trained model.

## Sliding Window Process

**1. Read in Embeddings:**
- Load the pre-computed embeddings for each token from a specified file. 
These embeddings serve as the foundational representations for training.

**2. Tokenization:**
- Each word, punctuation mark, or symbol in the input text is tokenized using the JTokkit library.
- JTokkit processes the text based on a dictionary, generating unique integer IDs for each token while preserving the context of punctuation and symbols.

**3. Window Generation:**
- A sliding window of size N (e.g., 4) is applied to the list of token IDs with a specified stride (e.g., 1).
- This creates overlapping windows of tokens, ensuring that context is retained across the training examples.

**4. Embedding Translation:**
- For each window of token IDs, the corresponding embeddings are retrieved from the previously loaded embeddings.
- This results in a structured dataset where each window is represented by its associated embeddings.

**5. Label Creation:**
- The last token in each sliding window serves as the target label for the model, while the preceding embeddings form the input feature set.
- The final dataset consists of input-target pairs suitable for supervised learning.

## Training Process

**1. Model Configuration:**
- Define a neural network architecture using DL4J, tailored to accept input shapes corresponding to the sliding window size.

**2. Training Loop:**
- Train the model over multiple epochs, minimizing the loss between predicted and actual target embeddings.
- Utilize DL4J’s Spark library for parallelization, allowing efficient handling of large datasets across distributed nodes.



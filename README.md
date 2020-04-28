<p align="center"><img width=12.5% src="https://github.com/KryptoComputerDudesMinusAdam/KryptoGame"></p>
<h1 align="center">Krypto Game</h1>
<p align="center">Computer Security Group Project</p>
<p align="center">By: Matthew Lee, Xavier LaRosa, Manohar Chitoda, Adam Chang, Malcom Akinseye and Shireen Ahmad</p>
<br>

## Objective

- The objective of this project is to illsutrate a simple, real life simulation of interactions between two messengers (Alice and Bob)
and an adversary (Chuck) attempting to infiltrate their conversations. The catch is that the conversations are encrypted with various (3) encryptions in which the adversary will have to decrypt.

## How to set up
- Server
  - When creating the server, simply type in a numerical input of any size (4 digits is recommended) and click connect.
  - The server must be created before anything else and the numerical input will be shared with ALL parties.
- Messenger (Alice and Bob)
  - If you are a messenger, you will need to input the designated numerical input in which was created by instantiating the server.
  - For the host name, keep it at its default value of "0.0.0.0"
  - Once connected, you will be given a list of users who are within the same server.
  - Click your desired partner and you will be able to chat with them through the messaging input after they have accepted your   invitation. 
- Attacker (Chuck)
  - Similar to the messengers, you too will require the input the numerical server input to connect to your desired server.
  - The main difference is you will also be required to input a certain setting indicating what kind of challenge you would undergo
  before your connection. 
  - The adversary has these following options:
    - Known-Plaintext Attack
    - Ciphertext Only Attack
    - Chosen Plaintext Attack
    - Chose Ciphertext Attack
  - The challenges are derived based upon what kind of information you want to be given as the adversary.
  - Once all inputs are filled, click connect
- Note: there can be more than 2 messengers as well as more than 1 attacker per server.
  
## Main Features
- Server
  - The server connects all parties towards once source and allows each user to interact with each other with their respective roles.
  - You are able to connect and disconnect the server.
- Messengers (Alice and Bob)
  - Once all prior set up has been completed, Alice and Bob are able to successfully send each other messages through the server
  - Messages received will be automatically encrypted and displayed as its encrypted message
  - There will be two text boxes displayed in the UI
    - TextBox 1: User can input their desired message and send to their recipeint by clicking the blue send button next to this box
    - TextBox 2: When a message is sent or recieved, click it followed by the green button next to this box. The decrypted message will appear within the original display of messages and its encyrption will be displayed here in TextBox 2.
  - Messengers will have the following options to encrypt their code:
    - Monalphabetic
    - Vigenere
    - Stream
    - RSA
- Attacker (Chuck)
  - Note: All attackers can run a query and an analysis but ONLY ONCE!
  - Known Plaintext
    - This settings allows the attacker to see both the plaintext and the ciphertext.
    - In order to view what messages have been set, simply click the "Query Server" button.
    - Once visible both plaintext and ciphertext will be accessible to analyze. 
    - You have a maximum of 5 pairs of text in which you can queue.
  - Ciphertext Only
    - As the name describes, you are only able to see the ciphertexts amongst the messengers.
    - 
    

## Technologies Used
- Intellij
- Scene Builder
- Github/Github Desktop

## Languages
- Java
- JavaFX

## Demo Of Applicaion
[![IMAGE ALT TEXT](https://github.com/KryptoComputerDudesMinusAdam/KryptoGame/blob/master/KryptoChat/application.png)](https://www.youtube.com/watch?v=5Ogsu51Gz60 "Krypto Chat")

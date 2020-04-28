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
  - The messengers have the following options to encrypt their conversations: 
    - Monalphabetic
    - Vigenere
    - Stream
    - RSA
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
- Note: There can be more than 2 messengers per server.
  
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
    - Monalphabetic: works by replacing each letter of the plaintext with another letter
    - Vigenere: uses 26 x 26 table for each letter of the alphabet as well as a key word in order to encrypt a given message 
    - Stream: the plaintext characters are encrypted one at a time where each character is dependent on the current state of the cipher.
    - RSA: an encrytion envolving the generation and distribution of a public key used to encrypt a desired message. 
- Attacker (Chuck)
  - Important Notes: 
    - All attackers can run a query and an analysis but ONLY ONCE!
    - All attackers can queue AT MOST 10 messages total.
  - Known Plaintext
    - This settings allows the attacker to see both the plaintext and the ciphertext.
    - In order to view what messages have been set, simply click the "Query Server" button.
    - Once visible both plaintext and ciphertext will be accessible to analyze. 
  - Ciphertext Only
    - As the name describes, you are only able to see the ciphertexts amongst the messengers.
    - Running the analysis the attacker will be able to view the frequency of letters.
    - You may also perform a brute force protcol that will test each letter of the alphabet. 
  - Chosen Known Plaintext
    - Choosing this will display two text boxes: one for the desired plaintext input and the other for the crypted output. 
    - The goal with this option is to be able to decrypt which ever encryption option that was chosen amongst the messengers.
  - Chosen Ciphertext
    - Similar to its counter part, this option will display the inverse of the known plaintext option.
    - In other words, you will input a ciphered text and receive its plain text on the second box
    - It excercises the same theme, being unable to view that actual conversation.
## Distribution of Works
- System Design
  - Messengers UI/Functionality
    - Xavier LaRosa
    - Matthew Lee
  - Attackers UI/Functionality
    - Manohar Chitoda
- Ciphers/Decryptions
  - Monoalphabetic
    - Shireen Ahmad
  - Vigenere
    - Malcom AKinseye
  - Stream:
    - Adam Chang
  - RSA: 
    - Xavier LaRosa
## Technologies Used
- Intellij
- Scene Builder
- Github/Github Desktop
## Languages
- Java
- JavaFX

## Demo of Application
[![Krypto Chat](https://github.com/KryptoComputerDudesMinusAdam/KryptoGame/blob/master/KryptoChat/application.png)](https://www.youtube.com/watch?v=SQIbeAk-bFA "Application")

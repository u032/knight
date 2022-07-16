# Knight
Chess Social Network,
[ui](https://knight.chesscord.wiki) 
([repo](https://github.com/u032/knight-ui)),
[docs](https://docs.chesscord.wiki),
[api](https://headcrab.gitbook.io/knight/)

## Instruction
1. Clone this repository.
```text
git clone https://github.com/u032/knight.git
```
2. Create project in Firebase.
3. Click to Settings icon and choose `Project settings`.
4. Click to tab `Service accounts`, choose `Firebase Admin SDK` and click button `Generate new private key`.
5. Rename `.env.example` to `.env` and write to this file required data (Don't forget specify path to the Firebase Admin SDK file).
6. Install OpenJDK 17.
```text
apt install openjdk-17-jdk
```
7. Run API.
```text
java -jar build/libs/knight.jar
```

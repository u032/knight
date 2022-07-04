# Knight
Chess Social Network,
[ui](https://github.com/u032/knight-ui),
[api](https://headcrabj.github.io/knight-api)

## Instruction
1. Clone this repository.
2. Create service account and generate key in GCP: [Instruction](https://cloud.google.com/docs/authentication/production#create_service_account).
3. Download this key and copy to this directory.
4. Rename `.env.example` to `.env` and write to this file required data.
5. Install docker and run `docker build -t kngiht .` command.
6. Run `docker run -d -p 8787:8787 knight` command.

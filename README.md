
# Alpaca Bot

Using the Alpaca trading API to create a bot automatically placing stock and crypto orders according to given strategies.

[![Maven CI](https://github.com/florianleca/alpaca-bot/actions/workflows/build.yml/badge.svg)](https://github.com/florianleca/alpaca-bot/actions/workflows/build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=florianleca_alpaca-bot&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=florianleca_alpaca-bot)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=florianleca_alpaca-bot&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=florianleca_alpaca-bot)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=florianleca_alpaca-bot&metric=bugs)](https://sonarcloud.io/summary/new_code?id=florianleca_alpaca-bot)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=florianleca_alpaca-bot&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=florianleca_alpaca-bot)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=florianleca_alpaca-bot&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=florianleca_alpaca-bot)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=florianleca_alpaca-bot&metric=coverage)](https://sonarcloud.io/summary/new_code?id=florianleca_alpaca-bot)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=florianleca_alpaca-bot&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=florianleca_alpaca-bot)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=florianleca_alpaca-bot&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=florianleca_alpaca-bot)
## Author

[@florianleca](https://github.com/florianleca)


## Environment Variables

To run this project, you will need to add the following environment variables to `src/main/resources/application-local.properties`:

Alpaca API keys:
- `ALPACA_API_KEY_ID`
- `ALPACA_API_SECRET_KEY`

MongoDB connection:
- `spring.data.mongodb.uri`
- `spring.data.mongodb.database`

## Run Locally

Once you have set the environment variables, you can build the project using Maven

```bash
  mvn clean install
```

Go to the target directory

```bash
  cd target
```

Run the .jar file

```bash
  java -jar alpaca-bot-[version].jar
```


## License

[![GPLv3 License](https://img.shields.io/badge/License-GPL%20v3-red.svg)](https://www.gnu.org/licenses/gpl-3.0.en.html)

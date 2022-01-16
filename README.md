# Smart-Agriculture

Alberto Moretti IoT project 2021/2022

# How to run this project

Create mosquitto image: `docker-compose up`

Launch demo files:

1. DataCollectorEmulator -> manager
2. LightControllerEmulator
3. IrrigationControllerEmulator
4. EnvironmentalMonitoringEmulator

# Costumize the demo

Open the listed files above and change the constants in top of the file.
In DataCollectorEmulator is also possible change `defaultIrrigationConfiguration` and `defaultLightConfiguration`.

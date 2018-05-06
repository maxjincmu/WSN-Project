# WSN-Project


## Motivation

According to the safety organization Kids and Cars, an average of 37 kids die each year in hot cars. Death in summers are prevalent due to increased heat. On August 5th, 2017, a grandmother left a 4-year old girl inside an SUV when she had thought that she dropped off the girl off at daycare. These types of situations where a parent or guardian forgets a child due to a lapse in memory is all too common. In 10 minutes, the temperature in a car can increase up to 20 degrees. Therefore, a wireless system in the car that promptly notifies the guardian about a left child is essential. 

## Setup

Master Node:
The master node is the node that controls the entire system in the car and interfaces with the outside world. The master node interfaces with the driver’s phone, the emergency phone, and the car’s alarm system to perform the necessary actions depending on the situation. 
The following are the components of the master node:
  A Raspberry Pi3:
    Communicates with the slaves using WiFi
    Communicates with the driver’s phone using BLE
    Communicates with the emergency phone using Google Assistant
  A weight sensor that reports the presence of the driver in the driver’s seat through SPI
  A temperature sensor that monitors the indoor car temperature


Slave Node:
The slave nodes are nodes that report the presence of babies in the car seat. The slave nodes consistently report baby presence information to the master node.
The following are the components of the slave node:
  An Arduino Uno WiFi:
    Performs processing on GridEye blob data as well as weight sensor reading
    Determines if baby is present based on sensor data and reports it to the master node
  A GridEye 8x8 Infrared Array sensor that is used to identify the baby
  A weight sensor to confirm the presence of a baby


## Contributors

Max Jin, Evaline Ju, Abhinand Sukumar

## Schedule

1.	All sensors sensing by end of March
2.	Sensors put in locations in the car that still give useful information by end of March
3.	Node communication by end of March
4.	Integration of all nodes in the system by end of April
5.	Ability to call emergency phone by end of April
6.	Phone application by end of April

## License




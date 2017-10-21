## Compile & run
/home/jdk1.8.0_144/bin/javac *.java & /home/jdk1.8.0_144/bin/java psi8GUI

## Execute agents
/home/jdk1.8.0_144/bin/java -cp "lib/jade.jar:out" jade.Boot -gui -agents prueba:agents.psi8MainAgent


## Questions
* ¿Cómo decido si uso DF o AMS para la búsqueda de agentes?
* ¿Búsqueda DF periódica?
* ¿Arrancar GUI desde MainAgent?
* ¿Clase Player?
* ¿Pasar main agent a gui o no?
* ¿El fixed agent escoge el mismo número de monedas y la misma apuesta? (La apuesta varía según los números anteriores)
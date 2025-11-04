\# proyecto tamagotchi con hilos (psp)



!\[hecho con: Java]

!\[asignatura: PSP]



esta es una simulacion de tamagotchis hecha en java para la asignatura de \*\*psp\*\* (programacion de servicios y procesos).



la idea es simular varios tamagotchis a la vez, donde \*\*cada tamagotchi es un hilo independiente\*\* que tiene su propia vida.



un "cuidador" (la clase `main`) los vigila y les da ordenes, pero los tamagotchis tambien hacen cosas por su cuenta.



---



\## funcionalidades



el proyecto tiene dos partes que corren a la vez: la vida de los tamas y las ordenes del cuidador.



\### vida autonoma (lo que hacen solos)

\* \*\*morir de viejos:\*\* duran 5 minutos de reloj.

\* \*\*ensuciarse solos:\*\* cada 20 segundos, la suciedad (`dirtiness`) sube +1.

\* \*\*morir de asco:\*\* si la suciedad llega a 10, el tamagotchi muere.

\* \*\*dar avisos:\*\* cuando la suciedad llega a 5, el tama te avisa de que empieza a oler mal.



\### menu del cuidador (lo que tu les mandas)

\* \*\*1. alimentar:\*\* los tamas tardan diferente tiempo en comer (uno 2s, otro 3s, etc).

\* \*\*2. limpiar:\*\* el baño dura 5 segundos y la suciedad (`dirtiness`) vuelve a 0.

\* \*\*3. jugar:\*\* el tamagotchi te pregunta una suma (dos numeros de 1 cifra, resultado < 10). si fallas, te pregunta otra hasta que aciertes.

\* \*\*4. ver estado:\*\* te dice si estan vivos, si estan "ociosos" y su nivel de suciedad.

\* \*\*5. matar:\*\* solo puedes matar a un tamagotchi si esta \*\*ocioso\*\* (si no esta comiendo, bañandose o jugando).

\* \*\*0. salir:\*\* apaga el programa mandando una interrupcion (`interrupt`) a todos los hilos.



---



\## como esta hecho (conceptos clave)



lo mas importante es como se comunican los hilos sin "pisarse" unos a otros.



\* `executorService`: se usa como el "mundo" que gestiona el pool de hilos. el cuidador le manda los `runnable` (los tamagotchis) y el se encarga de arrancarlos.



\* `blockingQueue` (el buzon): es la clave de todo. cada tamagotchi tiene un "buzon" (`actionqueue`).

&nbsp;   \* el \*\*cuidador\*\* (productor) echa una orden (`action.feed`) al buzon.

&nbsp;   \* el \*\*tamagotchi\*\* (consumidor) mira el buzon en su bucle `run()`.



\* `queue.poll(1, timeunit.seconds)`: este es el truco. el tamagotchi no se queda bloqueado esperando una orden. solo espera 1 segundo.

&nbsp;   \* si llega una orden, la hace.

&nbsp;   \* si no llega nada, pasa el segundo, `poll` devuelve `null` y el hilo sigue.

&nbsp;   \* esto nos permite ejecutar el `checklife()` (ensuciarse/envejecer) cada segundo.



\* `synchronized(scanner)`: para el juego. como solo hay una consola (`system.in`), teniamos que bloquearla. o la usa el cuidador para el menu, o la usa un tamagotchi para preguntar la suma. el `synchronized` es el "cerrojo" para que no hablen dos a la vez.



\* `volatile` (para `alive` e `idle`): una variable `volatile` obliga a los hilos a leerla siempre de la memoria principal (no de la cache). asi, cuando el cuidador pone `alive = false`, el hilo del tamagotchi se entera al instante.



\* `atomicinteger` (para `dirtiness`): es un contador "a prueba de hilos". `incrementandget()` es una operacion atomica, asi evitamos lios si varios hilos quisieran modificar la suciedad a la vez (aunque aqui no pasa, es una buena practica).



---



\## como ejecutarlo



1\.  asegurate de tener el \*\*jdk de java\*\* instalado.

2\.  pon los 3 ficheros (`cuidador.java`, `tamagotchi.java`, `action.java`) en la misma estructura de carpetas (ej. `org/cuatrovientos/dam/psp/tamagotchis/`).

3\.  compila desde la carpeta raiz:

&nbsp;   ```bash

&nbsp;   javac org/cuatrovientos/dam/psp/tamagotchis/\*.java

&nbsp;   ```

4\.  ejecuta la clase principal (`cuidador`):

&nbsp;   ```bash

&nbsp;   java org.cuatrovientos.dam.psp.tamagotchis.Cuidador

&nbsp;   ```

5\.  ¡juega con el menu!


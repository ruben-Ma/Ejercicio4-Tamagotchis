package org.cuatrovientos.dam.psp.tamagotchis;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;

/**
 * Representa un Tamagotchi.
 * cada intancia es un hilo.
 */
public class Tamagotchi implements Runnable {

    //  Atributos de Identidad y Estado 
    private final String name;
    private final int eatingSpeedMs;
    private volatile boolean alive;
    
    
    // volatile para que el Cuidador vea el cambio inmediatamente.
    private volatile boolean idle = true; // Empieza ocioso
    
    
    private final Scanner sharedScanner; // El scanner del Cuidador
    private final Random random;         // Para generar números

   
    private final AtomicInteger dirtiness; // la sucio de de tamagotchi 0-10
    private final BlockingQueue<Action> actionQueue; // "Buzón" thread-safe

   
    private long startTime;      
    private long ensuciarTama;   
    
    
    /**
     * Constructor del Tamagotchi 
     * @param name Nombre.
     * @param eatingSpeedMs Velocidad de comida.
     * @param sharedScanner El Scanner global (System.in) del Cuidador.
     */
    public Tamagotchi(String name, int eatingSpeedMs, Scanner sharedScanner) {
        this.name = name;
        this.eatingSpeedMs = eatingSpeedMs;
        this.sharedScanner = sharedScanner;
        this.random = new Random();
        this.alive = true;
        
        this.dirtiness = new AtomicInteger(0); //contador de suiciedad el tamagotchi
        this.actionQueue = new LinkedBlockingQueue<>();// buzon de ordenes que pondra el cuidador a la cola
    }

    
    
    private void handleAction(Action action) throws InterruptedException{
        
        System.out.println(" [" + name + "]  Recibida orden por el cuidador"+ action);
        
        switch(action) {
        
        case FEED: 
            System.out.println(" [" + name + "]  Empieza a comer");
            Thread.sleep(this.eatingSpeedMs); //comen a diferentes velocidades
            System.out.println(" [" + name + "]  Acaba de comer");
            break;
            
        case CLEAN: 
            System.out.println(" [" + name + "]  Empieza a bañarse");
            Thread.sleep(5000);//espera
            this.dirtiness.set(0);//contador a 0
            System.out.println(" [" + name + "]  Esta limpio");
            break;
            
        case PLAY: 
            synchronized (this.sharedScanner) {//mismo scanner
                playGame();//llama al metodojuego
            }
            break;
        }
    }
    
    
    private void playGame() {//juego
        if (!alive) return; //mira si esta vivo

        System.out.println("\n  [" + name + "]   A jugar con matesss");
        boolean acierto = false;

        while (!acierto && alive) {//si esta vivo y es cintratrio a falso
            int a = random.nextInt(10); 
            int b = random.nextInt(10);//dos numeros aleatorios
            
            if (a + b >= 10) continue; //que no sea as de 10 la suma
            
            int R_CORRECTA = a + b;
            System.out.print("\n>>> [" + name + "] pregunta: ¿Cuánto es " + a + " + " + b + "? ");
            
            int r_cuidador = -1;//valor negativo para que nunca se inice como correcta
            try {
                r_cuidador = sharedScanner.nextInt();
            } catch (InputMismatchException e) {
                 System.out.println("\n  [" + name + "] ¡Eso no es un número!");
                 sharedScanner.next(); // Limpiar buffer
                 continue; // Volver a preguntar
            }

            if (r_cuidador == R_CORRECTA) {// si las dos variables son iguales
                System.out.println("  [" + name + "] ¡Sí! ¡Correcto! ¡Qué divertido!");
                acierto = true;//acierto es diferente a false
            } else {
                System.out.println("  [" + name + "] ¡No! ¡Fallaste! Juguemos otra vez...");
            }
        }
        System.out.println("  [" + name + "]   ...terminé de jugar.");
        
        if (sharedScanner.hasNextLine()) {
            sharedScanner.nextLine();
        }
    }
     
    
    private void checkLife() {
        
        long now = System.currentTimeMillis();
        
        // Muerte por edad
        if( now - startTime > 300_000) { //muere pasado 5 mins
            System.out.println(" [" + name + "]  Mi tiempo ha pasado... voy a morir");
            this.alive = false;//cambia a falso
            return;
        }
        
        
        if( now - ensuciarTama > 20_000) {//cada 20 segundos
            this.ensuciarTama = now; 
            int currentDirt = dirtiness.incrementAndGet();//incrimenta la suciedad
            
            System.out.println(" [" + name + "]  Mi suciedad esta en "+ currentDirt);
                    
            if(currentDirt == 5) {//mensaje avso
                System.out.println(" [" + name + "] Ya empiezo a estar muy sucio... voy por la mitad antes de morir de guarro");
            }
                    
            if(currentDirt >= 10) {
                System.out.println(" [" + name + "] Ya que no me has lavado muero.... Malditooo");
                this.alive = false;
                return; 
            }
        }
    }
     
    
    @Override
    public void run() {//donde pasa toodo el hilo swl tamagotchi
        Thread.currentThread().setName("Tamagotchi-" + name);//nombra el hillo
        System.out.println(" ¡" + name + " ha nacido!");
        
        this.startTime = System.currentTimeMillis();//guarda la hora de nacimiento
        this.ensuciarTama = System.currentTimeMillis();//guarda cuanda se ensucia

        try {
            while (this.alive) {//bluce mientras esta vivp
                Action nextAction = actionQueue.poll(1, TimeUnit.SECONDS);//buzon donde el hilo recoge actions
                
                if (nextAction != null) {
                   
                    this.idle = false; //  Marcamos como OCUPADO
                    handleAction(nextAction);//ejecuta
                    this.idle = true;  // Marcamos como OCIOSO
                   
                }
                
                if (this.alive) {
                  checkLife();//comprueba lo sucio y que no esta muerto
                }
            }
        } catch (InterruptedException e) {
            this.alive = false;
            System.out.println(" [" + name + "] ha sido interrumpido.");
        }

        System.out.println(" --- " + name + " HA MUERTO. --- ");
    }
    
   

    public boolean isAlive() {
        return this.alive;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void feed() {
        this.actionQueue.offer(Action.FEED);
    }
    
    public void clean() {
        this.actionQueue.offer(Action.CLEAN);
    }
    
    public void play() {
        this.actionQueue.offer(Action.PLAY);
    }

   
    public boolean requestKill() {
        
        if (this.idle) {// si esta osocuio
            System.out.println(" [" + name + "]  El cuidador me pide morir. Adiós...");
            this.alive = false; // Acepta la muerte
            return true;
        } else {
            System.out.println(" [" + name + "]  ¡El cuidador quiere matarme pero estoy OCUPADO!");
            return false; // Rechaza la muerte
        }
    }
    
    
    public String getStatus() {
        // muwatra los datos en formato
        return String.format(" ->> %s | VIVo : %-5b | Ocioso: %-5b | Suciedad: %d/10", 
                             name, alive, idle, dirtiness.get());
    }
}
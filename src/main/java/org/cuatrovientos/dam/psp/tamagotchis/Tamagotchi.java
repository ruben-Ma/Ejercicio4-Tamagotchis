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
 * Cada instancia está diseñada para correr en su propio hilo.
 */
public class Tamagotchi implements Runnable {

   
    private final String name;
    private final int eatingSpeedMs;
    private volatile boolean alive;
    
   
    private final Scanner sharedScanner; // El scanner del Cuidador
    private final Random random;         // Para generar números

    
    private final AtomicInteger dirtiness; // la sucio de de tamagotchi 0-10
    // 'BlockingQueue' es el "buzón" thread-safe para recibir órdenes.
    private final BlockingQueue<Action> actionQueue;

    
    private long startTime;      // Cuándo nació (para la muerte por edad)
    private long lastDirtTick;   // Cuándo se ensució por última vez
    
    
    /**
     * Constructor del Tamagotchi (Actualizado).
     * @param name Nombre.
     * @param eatingSpeedMs Velocidad de comida.
     * @param sharedScanner El Scanner global (System.in) del Cuidador.
     */
    public Tamagotchi(String name, int eatingSpeedMs, Scanner sharedScanner) {
        this.name = name;
        this.eatingSpeedMs = eatingSpeedMs;
        this.sharedScanner = sharedScanner; // Añadido
        this.random = new Random();         // Añadido
        this.alive = true;
        
        
        this.dirtiness = new AtomicInteger(0);
        this.actionQueue = new LinkedBlockingQueue<>(); // Una cola enlazada, sin límite
    }

    
    
    private void handleAction(Action action) throws InterruptedException{
        
        System.out.println(" [" + name + "] (Accion) Recibida orden"+ action);
        
        
        switch(action) {
        
        case FEED: // Lógica de comida (existente)
            System.out.println(" [" + name + "] (Accion) Empieza a comer");
            Thread.sleep(this.eatingSpeedMs); //para que no coman igual
            System.out.println(" [" + name + "] (Accion) Acaba de comer");
            
            break;
            
        case CLEAN: // Lógica de limpieza (existente)
            System.out.println(" [" + name + "] (Accion) Empieza a bañarse");
            Thread.sleep(5000);
            this.dirtiness.set(0);
            System.out.println(" [" + name + "] (Accion) Esta limpio");
            break;
            
        case PLAY: 
            synchronized (this.sharedScanner) {
                playGame(); // <-- LLAMAR AL NUEVO MÉTODO
            }
            break;
        }
    }
    
    
    private void playGame() {
        if (!alive) return; // Por si acaso

        System.out.println("\n  [" + name + "] (Acción) 🎲 ¡Quiero jugar!");
        boolean acierto = false;

        while (!acierto && alive) {
            int a = random.nextInt(10); // 0-9
            int b = random.nextInt(10);
            
            if (a + b >= 10) continue; 
            
            int R_CORRECTA = a + b;
            System.out.print("\n>>> [" + name + "] pregunta: ¿Cuánto es " + a + " + " + b + "? ");
            
            int r_cuidador = -1;
            try {
                r_cuidador = sharedScanner.nextInt();
            } catch (InputMismatchException e) {
                 System.out.println("\n  [" + name + "] ¡Eso no es un número!");
                 sharedScanner.next(); // Limpiar buffer del scanner
                 continue; // Volver a preguntar
            }

            if (r_cuidador == R_CORRECTA) {
                System.out.println("  [" + name + "] ¡Sí! ¡Correcto! ¡Qué divertido!");
                acierto = true;
            } else {
                System.out.println("  [" + name + "] ¡No! ¡Fallaste! Juguemos otra vez...");
            }
        }
        System.out.println("  [" + name + "] (Acción) 🎲 ...terminé de jugar.");
        
        
        if (sharedScanner.hasNextLine()) {
            sharedScanner.nextLine();
        }
    }
     
    
    private void checkAutonomousStatus() {
        
        long now = System.currentTimeMillis();
        
        if( now - startTime > 300_000) { 
            System.out.println(" [" + name + "] (Vida) Mi tiempo ha pasado... voy a morir");
            this.alive = false;
            return;
        }
        
        if( now - lastDirtTick > 20_000) {
            this.lastDirtTick = now; 
            int currentDirt = dirtiness.incrementAndGet();
            
            System.out.println(" [" + name + "] (Vida) Mi suciedad esta en "+ currentDirt);
                    
            if(currentDirt == 5) {
                System.out.println(" [" + name + "] (Vida) Ya empiezo a estar muy sucio... voy por la mitad antes de morir de guarro");
            }
                    
            if(currentDirt >= 10) {
                System.out.println(" [" + name + "] (Vida) Me muero por que no me has lavado.... Malditooo");
                this.alive = false;
                return; 
            }
        }
    }
     
    
    @Override
    public void run() {
        Thread.currentThread().setName("Tamagotchi-" + name);
        
        System.out.println(" ¡" + name + " ha nacido!");
        
        
        this.startTime = System.currentTimeMillis();
        this.lastDirtTick = System.currentTimeMillis();

        try {
            while (this.alive) {
                
                Action nextAction = actionQueue.poll(1, TimeUnit.SECONDS);
                
                if (nextAction != null) {
                    handleAction(nextAction);
                }
                
                if (this.alive) {
                    checkAutonomousStatus();
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
    
    public String getStatus() {
        return String.format(" ->> %s | VIVo : %-5b | Suciedad: %d/10", name, alive, dirtiness.get());
    }
}
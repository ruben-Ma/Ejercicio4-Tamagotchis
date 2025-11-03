package org.cuatrovientos.dam.psp.tamagotchis;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Representa un Tamagotchi.
 * Cada instancia está diseñada para correr en su propio hilo.
 */
public class Tamagotchi implements Runnable {

    private final String name;

    
    private volatile boolean alive;
    
    
     //atriibutos de cocurrencia
      private final AtomicInteger dirtiness; // la sucio de de tamagotchi 0-10
     // 'BlockingQueue' es el "buzón" thread-safe para recibir órdenes.
      private final BlockingQueue<Action> actionQueue;

      // atributos de vida
      private long startTime;      // Cuándo nació (para la muerte por edad)
      private long lastDirtTick;   // Cuándo se ensució por última vez
    
    
      public Tamagotchi(String name) {
	    this.name = name;
	    this.alive = true;
	    
	    // nuevos atribustos del tamagotchi
	    this.dirtiness = new AtomicInteger(0);
	    this.actionQueue = new LinkedBlockingQueue<>(); // Una cola enlazada, sin límite
	}

    
    @Override
    public void run() {
        Thread.currentThread().setName("Tamagotchi-" + name);
        
        System.out.println(" ¡" + name + " ha nacido!");

        try {
        
            while (this.alive) {
                
           
                Thread.sleep(1000); 
                System.out.println("  [" + name + "] ...vivo...");

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
}

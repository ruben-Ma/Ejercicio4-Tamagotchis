package org.cuatrovientos.dam.psp.tamagotchis;

/**
 * Representa un Tamagotchi.
 * Cada instancia está diseñada para correr en su propio hilo.
 */
public class Tamagotchi implements Runnable {

    private final String name;

    
    private volatile boolean alive;
    
    
    public Tamagotchi(String name) {
        this.name = name;
        this.alive = true; // Nace vivo
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

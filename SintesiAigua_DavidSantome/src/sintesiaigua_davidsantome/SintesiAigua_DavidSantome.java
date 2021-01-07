package sintesiaigua_davidsantome;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author David Santomé Galván
 *
 * Enlace al vídeo: https://youtu.be/aEi3-vDTIIs
 *
 * Primera práctica evaluable de programación concurrente. En este ejercicio se
 * pretende simular el problema de la síntesis del agua con semáforos con y sin
 * interacalado.
 */
public class SintesiAigua_DavidSantome implements Runnable {

    // Procesos que intervienen y el número total de sintetizaciones
    static final int OXYGEN = 2;
    static final int HYDROGEN = 4;
    static final int TO_SYNTHESIZE = 4;

    // Semáforo para la sección crítica
    static Semaphore mutex = new Semaphore(1);
    // Semáforo para el turno oxígenos, no habrá permisos incialmente
    static Semaphore oxygenSemaphore = new Semaphore(0);
    // Semáforo para el turno hidrógenos, no habrá permisos inicialmente
    static Semaphore hydrogenSemaphore = new Semaphore(0);
    // Variable como contador de hidrógenos
    static volatile boolean odd = true;

    // Id del proceso
    int id;
    // Variable para indicar si se quiere realizar con o sin intercalado
    static boolean alternate;

    /**
     * @param id Tipo int
     *
     * Método para definir el id de cada proceso
     */
    public SintesiAigua_DavidSantome(int id) {
        this.id = id;
    }

    /**
     * Override del método run donde si se trata de un oxígeno realizará
     * el método "consumer()" y, en caso contrario, al ser hidrógeno realizará
     * "producer()_alternate" o "producer_Nalternate()" dependiendo de la 
     * opción "alternate" escogida.
     */
    @Override
    public void run() {
        if (id == 1 || id == 2) {
            consumer();
        } else {
            if (alternate) {
                producer_alternate();
            } else {
                producer_Nalternate();
            }
        }
    }

    /**
     * Método para los oxígenos donde una vez hayan llegado dos hidrógenos
     * podrá sintetizar y cuando haya terminado liberará a los dos hidrógenos
     * implicados en dicha sintetización.
     */
    public void consumer() {
        try {
            System.out.println("Aquest es l'Oxigen " + this.id);
            for (int i = 0; i < TO_SYNTHESIZE; i++) {

                // Espera a las dos moléculas de hidrógeno para sintetizar agua
                oxygenSemaphore.acquire();

                System.out.println("------------>L'Oxigen 0x" + this.id + " sintetitza aigua");

                // Imprime con delay para simular la sintetización 
                for (int j = 0; j < TO_SYNTHESIZE; j++) {
                    if (this.id == 1) {
                        System.out.print("*");
                    } else {
                        System.out.print("+");
                    }
                    Thread.sleep(500);
                }
                System.out.println("");

                // Libera a los dos hidrógenos utilizados en la sintetización
                hydrogenSemaphore.release(2);
            }

            // Termina el proceso
            System.out.println("L'Oxigen 0x" + this.id + " acaba");
        } catch (InterruptedException ex) {
            Logger.getLogger(SintesiAigua_DavidSantome.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Método para los hidrógenos con intercalado, el objetivo es liberar
     * al oxígeno y seguir permitiendo la entrada de hidrógenos a la SC.
     */
    public void producer_alternate() {
        // Delay para que lleguen primero los oxígenos
        try {
            Thread.sleep(4);
            System.out.println("        Aquest es l'Hidrogen " + this.id);
            Thread.sleep(4);

            for (int i = 0; i < TO_SYNTHESIZE; i++) {
                
                // Gestión del contador de hidrógenos
                mutex.acquire();

                // Operación de consulta del contador de hidrógenos
                if (odd) {

                    System.out.println("        L'Hidrogen senar " + this.id + " espera un altre hidrogen");

                    // Operación de depósito
                    odd = false;

                } else {

                    System.out.println("        L'Hidrogen parell " + this.id + " allibera un oxigen per fer aigua");

                    // Operación de depósito
                    odd = true;
                    
                    // Libera el oxígeno para sintetizar agua
                    oxygenSemaphore.release();
                }

                // Permite entrar en la SC al siguiente hidrógeno
                mutex.release();
                
                // Espera la sintetización
                hydrogenSemaphore.acquire();
                
                Thread.sleep(1);
            }

            // Termina el proceso
            System.out.println("        L'Hidrogen " + this.id + " acaba");
        } catch (InterruptedException e) {
        }
    }

    /**
     * Método para los hidrógenos sin intercalado, el objetivo es liberar
     * al oxígeno y no permitir la entrada a más hidrógenos hasta que termine
     * la sintetización.
     */
    public void producer_Nalternate() {
        // Delay para que lleguen primero los oxígenos
        try {
            Thread.sleep(4);
            System.out.println("        Aquest es l'Hidrogen " + this.id);
            Thread.sleep(4);

            for (int i = 0; i < TO_SYNTHESIZE; i++) {

                // Gestión del contador de hidrógenos
                mutex.acquire();

                // Operación de consulta del contador de hidrógenos
                if (odd) {

                    System.out.println("        L'Hidrogen senar " + this.id + " espera un altre hidrogen");

                    // Operación de depósito
                    odd = false;

                    // Permite entrar en la SC al siguiente hidrógeno
                    mutex.release();

                    // Espera la sintetización
                    hydrogenSemaphore.acquire();

                } else {

                    System.out.println("        L'Hidrogen parell " + this.id + " allibera un oxigen per fer aigua");

                    // Operación de depósito
                    odd = true;

                    // Libera el oxígeno para sintetizar agua
                    oxygenSemaphore.release();

                    // Espera la sintetización
                    hydrogenSemaphore.acquire();

                    // Permite entrar en la SC al siguiente hidrógeno
                    mutex.release();
                }
            }
            // Termina el proceso
            System.out.println("        L'Hidrogen " + this.id + " acaba");
        } catch (InterruptedException e) {
        }
    }

    /**
     * @param args
     * @throws java.lang.InterruptedException
     * 
     * Método para elegir la opción mediante un menú y el lanzamiento de los
     * threads oxígenos e hidrógenos.
     */
    public static void main(String[] args) throws InterruptedException {

        // Menú para elegir intercalado, sin intercalado o salir 
        boolean sortir = false;
        int opcio;
        while (!sortir) {
            menu();
            opcio = readNum("\n\tInserir opció: ");
            switch (opcio) {
                case 1:
                    alternate = true;
                    break;
                case 2:
                    alternate = false;
                    break;
                case 0:
                    sortir = true;
                    return;
                default:
                    break;
            }

            System.out.println("\nSimulació Sintetització Aigua");
            int t = 0;
            Thread[] threads = new Thread[OXYGEN + HYDROGEN];

            // Iniciamos los consumers -> oxígenos  
            int i;
            for (i = 0; i < OXYGEN; i++) {
                threads[t] = new Thread(new SintesiAigua_DavidSantome(t + 1));
                threads[t].start();
                t++;
            }

            // Iniciamos los producers -> hidrógenos 
            for (i = 0; i < HYDROGEN; i++) {
                threads[t] = new Thread(new SintesiAigua_DavidSantome(t + 1));
                threads[t].start();
                t++;
            }

            // Esperamos a todos los procesos 
            for (i = 0; i < OXYGEN + HYDROGEN; i++) {
                threads[i].join();
            }
        }
    }

    /**
     * Método para imprimir por pantalla las diferentes opciones del programa.
     */
    private static void menu() {
        System.out.println("\n\nGESTIÓN PROGRAMA SINTESIS DE AGUA");
        System.out.println("\n\t1. Con intercalado");
        System.out.println("\t2. Sin intercalado");
        System.out.println("\t0. Salir");
    }

    /**
     * @param msg Tipo String
     *
     * Método para obtener por teclado el número que se utilizará para definir
     * la opción elegida.
     */
    private static int readNum(String msg) {
        int x = 0;
        try {
            String s;
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            System.out.print(msg);
            s = in.readLine();
            x = Integer.parseInt(s);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return x;
    }
}

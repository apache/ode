package org.apache.ode.scheduler.simple;

/**
 * The thing that runs the scheduled tasks.
 * 
 * @author Maciej Szefler ( m s z e f l e r @ g m a i l . c o m )
 *
 */
interface TaskRunner {

    
    public void runTask(Task task);
}

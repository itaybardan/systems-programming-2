package bgu.spl.mics.example.services;


import bgu.spl.mics.MicroService;


public class ExampleMicroService extends MicroService {
    /**
     * @param name the microservice name (used mainly for debugging purposes -
     *             does not have to be unique)
     */


    public ExampleMicroService(String name) {
        super(name);
    }

    @Override
    public synchronized void notifyMicroService() {
        notify();
    }

    @Override
    public synchronized void initialize() {//Only synchronized for this test!
        try {
            wait();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}


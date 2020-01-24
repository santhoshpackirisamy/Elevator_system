import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.TreeSet;

public class Elavator_system {

    public static void main(String[] args) {
        System.out.println("Welcome to MyLift");
        Thread requestListenerThread = new Thread(new RequestListener(), "RequestListenerThread");
        Thread requestProcessorThread = new Thread(new RequestProcessor(), "RequestProcessorThread");

//        Elevator.getInstance().setRequestProcessorThread(requestProcessorThread);

        Elevator elevator1 = new Elevator();
        Elevator elevator2 = new Elevator();

        ArrayList<Elevator> elevatorArrayList = new ArrayList<Elevator>();

        elevatorArrayList.add(elevator1);
        elevatorArrayList.add(elevator2);

        Elevator.assignInstance(elevatorArrayList);

        elevator1.setRequestProcessorThread(requestProcessorThread);
        elevator2.setRequestProcessorThread(requestProcessorThread);

        requestListenerThread.start();
        requestProcessorThread.start();


    }
}

class Elevator {

    public boolean status = false;
    private static Elevator elevator = null;

    private static TreeSet requestSet = new TreeSet();

    private int currentFloor = 0;

    private Direction direction = Direction.UP;

//    private Elevator() {};

    private Thread requestProcessorThread;


    static ArrayList<Elevator> arrayListelevator;

    static void assignInstance(ArrayList<Elevator> elevatorArrayList)
    {
        arrayListelevator = new ArrayList<Elevator>(elevatorArrayList);
    }


    static Elevator getInstance() {
        Elevator setelevator=null;
        int prevfloor=100;
        //       if (elevator == null) {
//            elevator = new Elevator();
        for(int i=0;i<2;i++) {
            elevator = arrayListelevator.get(i);

            //     System.out.println(elevator);
            if (elevator.currentFloor < prevfloor) {
//                    System.out.println("---------");
                setelevator = elevator;
                prevfloor = elevator.currentFloor;
            }
        }
        //      }
        if(setelevator != null)
        {
            elevator = setelevator;
        }
//        System.out.println("---------" + elevator);
        return elevator;
    }

    public synchronized void addFloor(int f) {
        requestSet.add(f);

        if(requestProcessorThread.getState() == Thread.State.WAITING){
            notify();
        }else{
            requestProcessorThread.interrupt();
        }

    }

    public synchronized int nextFloor() {

        Integer floor = null;

        if (direction == Direction.UP) {
            if (requestSet.ceiling(currentFloor) != null) {
                floor = (Integer) requestSet.ceiling(currentFloor);
            } else {
                floor = (Integer) requestSet.floor(currentFloor);
            }
        } else {
            if (requestSet.floor(currentFloor) != null) {
                floor = (Integer) requestSet.floor(currentFloor);
            } else {
                floor = (Integer) requestSet.ceiling(currentFloor);
            }
        }
        if (floor == null) {
            try {
                Elevator elevatorinstance,elevatorwaitinstance;
                System.out.println("Waiting at Floor :" + getCurrentFloor());
                this.status=false;

                for(int i=0;i<2;i++) {
                    elevatorinstance = arrayListelevator.get(i);
                    if(elevatorinstance.status==true)
                    {
                        requestProcessorThread = elevatorinstance.getRequestProcessorThread();
                        wait();
                        requestProcessorThread.interrupt();
                    }
                }
                wait();
                //    Thread.sleep(1000);
//                requestProcessorThread.run();
                //requestProcessorThread.interrupt();

            } catch (InterruptedException e) {
                System.out.println("%%%%%%%%%%%");
                // e.printStackTrace();
            }
        } else {
            requestSet.remove(floor);
        }
        return (floor == null) ? -1 : floor;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public void setCurrentFloor(int currentFloor) throws InterruptedException {
        if (this.currentFloor > currentFloor) {
            setDirection(Direction.DOWN);
        } else {
            setDirection(Direction.UP);
        }
        this.currentFloor = currentFloor;

        System.out.println("Floor : " + currentFloor);

        Thread.sleep(1000);
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Thread getRequestProcessorThread() {
        return requestProcessorThread;
    }

    public void setRequestProcessorThread(Thread requestProcessorThread) {
        this.requestProcessorThread = requestProcessorThread;
    }

    public TreeSet getRequestSet() {
        return requestSet;
    }

    public void setRequestSet(TreeSet requestSet) {
        this.requestSet = requestSet;
    }

}

class RequestProcessor implements Runnable {

    @Override
    public void run() {
        while (true) {
            Elevator elevator = Elevator.getInstance();
            elevator.status=true;
            System.out.println(Elevator.getInstance());
            int floor = elevator.nextFloor();
            int currentFloor = elevator.getCurrentFloor();
            try{
                if (floor >= 0) {
                    if (currentFloor > floor) {
                        while (currentFloor > floor) {
                            elevator.setCurrentFloor(--currentFloor);
                        }
                    } else {
                        while (currentFloor < floor) {
                            elevator.setCurrentFloor(++currentFloor);
                        }
                    }
                    System.out.println("Welcome to Floor : " + elevator.getCurrentFloor());
                    Thread.sleep(3000);
                }

            }catch(InterruptedException e){
                if(elevator.getCurrentFloor() != floor){
                    elevator.getRequestSet().add(floor);
                }
            }
        }
    }
}

class RequestListener implements Runnable {

    @Override
    public void run() {

        while (true) {
            String floorNumberStr = null;
            try {
                // Read input from console
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
                floorNumberStr = bufferedReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (isValidFloorNumber(floorNumberStr)) {
                System.out.println("User Pressed : " + floorNumberStr);
                Elevator elevator = Elevator.getInstance();
                elevator.addFloor(Integer.parseInt(floorNumberStr));
            } else {
                System.out.println("Floor Request Invalid : " + floorNumberStr);
            }
        }
    }

    private boolean isValidFloorNumber(String s) {
        return (s != null) && s.matches("\\d{1,2}");
    }

}

enum Direction {
    UP, DOWN
} 
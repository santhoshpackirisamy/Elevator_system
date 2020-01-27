import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.TreeSet;

public class Elavator_system {

    public static void main(String[] args) {
        System.out.println("Welcome to MyLift");

        ArrayList<Elevator> ElevatorInstance = new ArrayList<Elevator>();

        Thread requestListenerThread = new Thread(new RequestListener(),"RequestListenerThread");

        Thread requestProcessorThread1 = new Thread(new RequestProcessor(),"RequestProcessorThreadforLift1");
        Thread requestProcessorThread2 = new Thread(new RequestProcessor(),"RequestProcessorThreadforLift2");
        Thread requestProcessorThread3 = new Thread(new RequestProcessor(),"RequestProcessorThreadforLift3");

        Elevator elevator1 = new Elevator();
        Elevator elevator2 = new Elevator();
        Elevator elevator3 = new Elevator();

        ElevatorInstance.add(elevator1);
        ElevatorInstance.add(elevator2);
        ElevatorInstance.add(elevator3);

        elevator1.setRequestProcessorThread(requestProcessorThread1);
        elevator2.setRequestProcessorThread(requestProcessorThread2);
        elevator3.setRequestProcessorThread(requestProcessorThread3);

        Elevator.setElevator(ElevatorInstance);

//        System.out.println(Elevator.getInstance().getRequestProcessorThread().getName());

        requestListenerThread.start();
        requestProcessorThread1.start();
        requestProcessorThread2.start();
        requestProcessorThread3.start();
        
    }
}

class Elevator {

    private static Elevator elevator = null;

    private static TreeSet requestSet = new TreeSet();

    private int currentFloor = 0;

    private Direction direction = Direction.UP;

//    private Elevator() {};

    static ArrayList<Elevator> ElevatorInstance;

    private Thread requestProcessorThread;

    public static void setElevator(ArrayList<Elevator> ElevatorInstance)
    {
         Elevator.ElevatorInstance = new ArrayList<Elevator>(ElevatorInstance);
    }

    static Elevator getInstance() {
        if (elevator == null) {
//            elevator = new Elevator();
            elevator = Elevator.ElevatorInstance.get(0);
        }
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
                System.out.println("Waiting at Floor :" + getCurrentFloor());
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("removed" + floor);
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

        Thread.sleep(3000);
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
            int nextfloor = elevator.nextFloor();
            int currentFloor = elevator.getCurrentFloor();
            try{
                if (nextfloor >= 0) {
                    if (currentFloor > nextfloor) {
                        while (currentFloor > nextfloor) {
                            elevator.setCurrentFloor(--currentFloor);
                        }
                    } else {
                        while (currentFloor < nextfloor) {
                            elevator.setCurrentFloor(++currentFloor);
                        }
                    }
                    System.out.println("Welcome to Floor : " + elevator.getCurrentFloor());
                }

            }catch(InterruptedException e){
//                System.out.println(e);
                if(elevator.getCurrentFloor() != nextfloor){
                    System.out.println("added "+nextfloor);
                    elevator.getRequestSet().add(nextfloor);
                }
            }
        }
    }
}

class RequestListener implements Runnable {

    @Override
    public void run() {

        while (true)
        {
                String floorNumberStr = null;
            try {
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

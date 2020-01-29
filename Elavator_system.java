import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.TreeSet;

import static java.lang.Math.E;
import static java.lang.Math.abs;

public class Elavator_system {

    public static void main(String[] args) {
        System.out.println("Welcome to MyLift");

        ArrayList<Elevator> ElevatorInstance = new ArrayList<Elevator>();

        Thread requestListenerThread = new Thread(new RequestListener(),"RequestListenerThread");

        Thread requestProcessorThread1 = new Thread(new RequestProcessor(),"Lift1");
        Thread requestProcessorThread2 = new Thread(new RequestProcessor(),"Lift2");
        Thread requestProcessorThread3 = new Thread(new RequestProcessor(),"Lift3");
        Thread requestProcessorThread4 = new Thread(new RequestProcessor(),"Lift4");

        Elevator elevator1 = new Elevator(Type.ALL);
        Elevator elevator2 = new Elevator(Type.ALL);
        Elevator elevator3 = new Elevator(Type.ODD);
        Elevator elevator4 = new Elevator(Type.EVEN);
        ElevatorInstance.add(elevator1);
        ElevatorInstance.add(elevator2);
        ElevatorInstance.add(elevator3);
        ElevatorInstance.add(elevator4);

        elevator1.setRequestProcessorThread(requestProcessorThread1);
        elevator2.setRequestProcessorThread(requestProcessorThread2);
        elevator3.setRequestProcessorThread(requestProcessorThread3);
        elevator4.setRequestProcessorThread(requestProcessorThread4);

        Elevator.setElevator(ElevatorInstance);

        requestListenerThread.start();
        requestProcessorThread1.start();
        requestProcessorThread2.start();
        requestProcessorThread3.start();
        requestProcessorThread4.start();

    }
}

class Elevator implements ElevatorSystemInterface{

    private static Elevator elevator = null;

    public TreeSet requestSet = new TreeSet();

    private int currentFloor = 0;

    private Direction direction = Direction.UP;

    private Type Type;

    static ArrayList<Elevator> ElevatorInstance;

    private Thread requestProcessorThread;

    public Elevator(Type type) {
        Type = type;
    }

    public static void setElevator(ArrayList<Elevator> ElevatorInstance)
    {
        Elevator.ElevatorInstance = new ArrayList<Elevator>(ElevatorInstance);
    }

    @Override
    public synchronized void addFloor(int floor) {

        Elevator elevatoritr = null;
        Elevator bestelevator = null;
        int floordifference=100;

        for(int i=0;i<Elevator.ElevatorInstance.size();i++)
        {
            int curflr=0;
            elevatoritr = Elevator.ElevatorInstance.get(i);
            curflr = elevatoritr.getCurrentFloor();
            if(floor > curflr && elevatoritr.direction == Direction.UP && ( (floor%2==0? Type.EVEN : Type.ODD) == elevatoritr.getType() ) )
            {
                int tempfloordifference = floor-curflr;
                if(tempfloordifference < floordifference)
                {
                    floordifference = tempfloordifference;
                    bestelevator = elevatoritr;
                }
            }
            else if (floor < curflr && elevatoritr.direction == Direction.DOWN && ( (floor%2==0? Type.EVEN : Type.ODD) == elevatoritr.getType() ))
            {
                int tempfloordifference = curflr-floor;
                if(tempfloordifference < floordifference)
                {
                    floordifference = tempfloordifference;
                    bestelevator = elevatoritr;
                }
            }
            else if( (elevatoritr.getRequestProcessorThread().getState() == Thread.State.WAITING) && ( (floor%2==0? Type.EVEN : Type.ODD) == elevatoritr.getType() ))
            {
                int tempfloordifference = abs(curflr-floor);
                if(tempfloordifference < floordifference)
                {
                    floordifference = tempfloordifference;
                    bestelevator = elevatoritr;
                }
            }
        }


        for(int i=0;i<Elevator.ElevatorInstance.size();i++)
        {
            int curflr=0;
            elevatoritr = Elevator.ElevatorInstance.get(i);
            curflr = elevatoritr.getCurrentFloor();
            if(elevatoritr.requestSet.isEmpty() && elevatoritr.getType() == Type.ALL)
            {
                int tempfloordifference = abs(curflr-floor);
                if(tempfloordifference*(1.5) < floordifference)
                {
                    floordifference = tempfloordifference;
                    bestelevator = elevatoritr;
                }
            }
        }

        System.out.println(bestelevator.getRequestProcessorThread().getName()+ " is best Elevator");

        if(bestelevator==null)
        {
            for(int i=0;i<Elevator.ElevatorInstance.size();i++)
            {
                elevatoritr = Elevator.ElevatorInstance.get(i);
                if(elevatoritr.getRequestProcessorThread().getState() == Thread.State.WAITING)
                {
                    bestelevator = elevatoritr;
                }
            }
        }

        bestelevator.requestSet.add(floor);

        if(requestProcessorThread.getState() == Thread.State.WAITING){
            notify();
        }else{
            requestProcessorThread.interrupt();
        }

    }

    @Override
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
                int elevatorwaitingcount = 0;
                for(int i=0;i<Elevator.ElevatorInstance.size();i++) {
                    if ((Elevator.ElevatorInstance.get(i).requestSet.isEmpty())) {
                        elevatorwaitingcount++;
                    }
                }
                if(elevatorwaitingcount==Elevator.ElevatorInstance.size())
                {
                    for(int i=0;i<Elevator.ElevatorInstance.size();i++) {
                        System.out.println(Elevator.ElevatorInstance.get(i).getRequestProcessorThread().getName() + " Waiting at Floor :" + Elevator.ElevatorInstance.get(i).getCurrentFloor());
                    }
                }

                for(int i=0;i<Elevator.ElevatorInstance.size();i++) {
                    if ((!Elevator.ElevatorInstance.get(i).requestSet.isEmpty()) && Elevator.ElevatorInstance.get(i).requestProcessorThread.getState() == Thread.State.WAITING) {
                        System.out.println(Elevator.ElevatorInstance.get(i).requestProcessorThread.getName() + " is started");
                        Elevator.ElevatorInstance.get(i).requestProcessorThread.interrupt();
                    }
                }
                wait();
            } catch (InterruptedException e) {
           //     e.printStackTrace();
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

        System.out.println(requestProcessorThread.getName() + " Floor : " + currentFloor);

        Thread.sleep(2000);
    }

    public Direction getDirection() {
        return direction;
    }


    public void setType(Type Type) {
        this.Type = Type;
    }

    public Type getType() {
        return Type;
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

            Elevator elevator = null;

            for(int i=0;i<Elevator.ElevatorInstance.size();i++) {
                if (Elevator.ElevatorInstance.get(i).getRequestProcessorThread().getState() != Thread.State.WAITING) {
                    elevator = Elevator.ElevatorInstance.get(i);
                }
            }

            if (elevator==null)
            {
                elevator=Elevator.ElevatorInstance.get(0);
            }

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
                    System.out.println(elevator.getRequestProcessorThread().getName() + " Welcome to Floor : " + elevator.getCurrentFloor());
                }

            }catch(InterruptedException e){
                if(elevator.getCurrentFloor() != nextfloor){
//                    System.out.println("added "+nextfloor);
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

                Elevator elevator = null;
                for(int i=0;i<Elevator.ElevatorInstance.size();i++) {
                    if (Elevator.ElevatorInstance.get(i).getRequestProcessorThread().getState() != Thread.State.WAITING) {
                        elevator = Elevator.ElevatorInstance.get(i);
                    }
                }

                if(elevator==null)
                {
                    elevator = Elevator.ElevatorInstance.get(0);
                }
                elevator.addFloor(Integer.parseInt(floorNumberStr));

                elevator.getRequestProcessorThread().interrupt();
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

enum Type {
    ALL, ODD , EVEN
}

interface ElevatorSystemInterface
{
    void addFloor(int floor);
    int nextFloor();
}
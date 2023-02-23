import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Random;

public class ServerManager {
    private int port;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private String myMap;
    private int myShipsLeft;
    private Random random;
    private ArrayList<Integer> unknownFields;
    private ArrayList<Integer> waterFields;
    private ArrayList<Integer> priorityFields; // possible field with ship, near to destroyed ship part
    private ArrayList<Integer> shipFields;
    private ArrayList<Integer> currentShip; // current ship which we attack, we need this array to be able
                                            // to sign all near fields as water, after this ship is destroyed


    public ServerManager(int port, String mapFile){
        this.port = port;
        try {
            this.myMap = new BufferedReader(new FileReader(mapFile)).readLine();
            this.unknownFields = new ArrayList<>();
            this.waterFields = new ArrayList<>();
            this.priorityFields = new ArrayList<>();
            this.shipFields = new ArrayList<>();
            this.currentShip = new ArrayList<>();
            this.myShipsLeft = 20;
            random = new Random();
            for(int i=0; i<100; i++) this.unknownFields.add(i);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startGame() {
        DataOutputStream dataOutputStream = null;
        String myFeedback = "bad command received";
        int myShot = -99999;
        int errorCounter = 0;
        try {
            System.out.println("Game starts! Your map:");
            printMyMap();
            serverSocket = new ServerSocket(port);
            System.out.println("[server] server started. waiting for connection.");
            clientSocket = serverSocket.accept();
            clientSocket.setSoTimeout(1000);
            serverSocket.setSoTimeout(1000);
            System.out.println("[server] client connected");
            OutputStream outputStream = clientSocket.getOutputStream();
            dataOutputStream = new DataOutputStream(outputStream);
            InputStream inputStream = clientSocket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);

            String enemyShotStr;
            int enemyShot;

            while (true) {
                if (myShot != 99999) System.out.println("you shoot " + intToCoords(myShot));
                String responseFromEnemy = dataInputStream.readUTF();
                // proceed responseFromEnemy
                String feedbackFromEnemy = responseFromEnemy.split(";")[0]; // can be pudlo, trafiony, ...
                if(errorCounter>3){
                    ExitHandler.exit(1,"Blad komunikacji");
                }
                boolean badCommand = false;
                if (!feedbackFromEnemy.equals("ostatni zatopiony\n")) {
                    enemyShotStr = responseFromEnemy.split(";")[1].split("\n")[0];
                    enemyShot = coordsToInt(enemyShotStr);
                    switch (feedbackFromEnemy) {
                        case "start":
                            break;
                        case "pudlo":
                            System.out.println("pudlo!");
                            waterFields.add(myShot);
                            unknownFields.remove(Integer.valueOf(Integer.valueOf(myShot)));
                            priorityFields.remove(Integer.valueOf(myShot));
                            break;
                        case "trafiony":
                            System.out.println("trafiony!");
                            shipFields.add(myShot);
                            currentShip.add(myShot);
                            unknownFields.remove(Integer.valueOf(myShot));
                            priorityFields.remove(Integer.valueOf(myShot));
                            if (myShot % 10 != 9 && unknownFields.contains(myShot + 1)) priorityFields.add(myShot + 1);
                            if (myShot % 10 != 0 && unknownFields.contains(myShot - 1)) priorityFields.add(myShot - 1);
                            if (myShot + 10 <= 99 && unknownFields.contains(myShot + 10))
                                priorityFields.add(myShot + 10);
                            if (myShot - 10 >= 0 && unknownFields.contains(myShot - 10))
                                priorityFields.add(myShot - 10);
                            break;
                        case "trafiony zatopiony":
                            System.out.println("trafiony zatopiony!");
                            shipFields.add(myShot);
                            currentShip.add(myShot);
                            unknownFields.remove(Integer.valueOf(myShot));
                            priorityFields.clear();
                            for (int shipPart : currentShip) {
                                if (shipPart % 10 != 9 && unknownFields.contains(shipPart + 1)) {
                                    waterFields.add(shipPart + 1);
                                    unknownFields.remove(Integer.valueOf(shipPart + 1));
                                }
                                if (shipPart % 10 != 0 && unknownFields.contains(shipPart - 1)) {
                                    waterFields.add(shipPart - 1);
                                    unknownFields.remove(Integer.valueOf(shipPart - 1));
                                }
                                if (shipPart + 10 <= 99 && unknownFields.contains(shipPart + 10)) {
                                    waterFields.add(shipPart + 10);
                                    unknownFields.remove(Integer.valueOf(shipPart + 10));
                                }
                                if (shipPart - 10 >= 0 && unknownFields.contains(shipPart - 10)) {
                                    waterFields.add(shipPart - 10);
                                    unknownFields.remove(Integer.valueOf(shipPart - 10));
                                }
                                if (shipPart - 10 >= 0 && shipPart % 10 != 9 && unknownFields.contains(shipPart - 10 + 1)) {
                                    waterFields.add(shipPart - 10 + 1);
                                    unknownFields.remove(Integer.valueOf(shipPart - 10 + 1));
                                }
                                if (shipPart - 10 >= 0 && shipPart % 10 != 0 && unknownFields.contains(shipPart - 10 - 1)) {
                                    waterFields.add(shipPart - 10 - 1);
                                    unknownFields.remove(Integer.valueOf(shipPart - 10 - 1));
                                }
                                if (shipPart + 10 <= 99 && shipPart % 10 != 9 && unknownFields.contains(shipPart + 10 + 1)) {
                                    waterFields.add(shipPart + 10 + 1);
                                    unknownFields.remove(Integer.valueOf(shipPart + 10 + 1));
                                }
                                if (shipPart + 10 <= 99 && shipPart % 10 != 0 && unknownFields.contains(shipPart + 10 - 1)) {
                                    waterFields.add(shipPart + 10 - 1);
                                    unknownFields.remove(Integer.valueOf(shipPart + 10 - 1));
                                }
                            }
                            currentShip.clear();
                            break;
                        default:
                            //W razie otrzymania niezrozumialej komendy nalezy ponownie wyslac swoja ostatnia wiadomosc
                            badCommand = true;
                            errorCounter++;
                            break;
                    }
                    if (badCommand == false) {
                        if (priorityFields.isEmpty() == false)
                            myShot = priorityFields.get(random.nextInt(priorityFields.size()));
                        else
                            myShot = unknownFields.get(random.nextInt(unknownFields.size()));

                        System.out.println("enemy shoots " + intToCoords(enemyShot));
                        if (myMap.charAt(enemyShot) == '.' || myMap.charAt(enemyShot) == '~') {
                            myMap = setCharAtPosition(myMap, enemyShot, '~');
                            myFeedback = "pudlo";
                        }
                        if (myMap.charAt(enemyShot) == '#') myShipsLeft--;
                        if (myShipsLeft == 0) {
                            if (myMap.charAt(enemyShot) == '#' || myMap.charAt(enemyShot) == '@') {
                                myMap = setCharAtPosition(myMap, enemyShot, '@');
                                if (myWholeShipIsDestroyed(enemyShot, myMap))
                                    myFeedback = "trafiony zatopiony";
                                else myFeedback = "trafiony";
                            }
                            System.out.println("Twoj ostatni statek zatopiony. YOU LOST.");
                            System.out.println("Przegrana");
                            System.out.println("Mapa przeciwnika:");
                            printEnemyMap();
                            dataOutputStream.writeUTF("ostatni zatopiony\n");
                            break; // we lost...
                        } else if (myMap.charAt(enemyShot) == '#' || myMap.charAt(enemyShot) == '@') {
                            myMap = setCharAtPosition(myMap, enemyShot, '@');
                            if (myWholeShipIsDestroyed(enemyShot, myMap))
                                myFeedback = "trafiony zatopiony";
                            else myFeedback = "trafiony";
                        }
                        System.out.println(myFeedback);

                        printBothMaps();
                    }
                    dataOutputStream.writeUTF(myFeedback + ";" + intToCoords(myShot) + "\n");
                    dataOutputStream.flush();

                } else { // if ostatni zatopiony
                    System.out.println("ostatni zatopiony! YOU WON!");
                    System.out.println("Wygrana");
                    System.out.println("Pelna mapa przeciwnika:");
                    printFullEnemyMap();
                    break;
                }
                //System.out.println("your map:");
                //printMyMap();
                //System.out.println("enemy map:");
                //printEnemyMap();
            }
            System.out.println("both final maps:");
            printBothMaps();
            System.out.println("[server] Game ended. exiting and closing everything");
            //System.out.println("your map:");
            //printMyMap();
            //System.out.println("enemy map:");
            //printEnemyMap();
            dataOutputStream.close();
            clientSocket.close();
            serverSocket.close();
        } catch (SocketTimeoutException e) {
            errorCounter++;
            try {
                dataOutputStream.writeUTF(myFeedback + ";" + intToCoords(myShot) + "\n");
                dataOutputStream.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printFullEnemyMap(){
        String resultMap = "####################################################################################################";
        for (int unknownField : unknownFields) resultMap = setCharAtPosition(resultMap,unknownField, '.');
        for (int waterField : waterFields) resultMap = setCharAtPosition(resultMap,waterField, '.');
        System.out.println("   ABCDEFGHIJ");
        for(int i=0; i<10; i++){
            if (i==9) System.out.print((i+1)+" ");
            else System.out.print((i+1)+"  ");
            for (int j=0; j<10; j++){
                System.out.print(resultMap.charAt(j*10 + i));
            }
            System.out.println();
        }
    }

    private void printEnemyMap(){
        String resultMap = "####################################################################################################";
        for (int unknownField : unknownFields) resultMap = setCharAtPosition(resultMap,unknownField, '?');
        for (int waterField : waterFields) resultMap = setCharAtPosition(resultMap,waterField, '.');
        System.out.println("   ABCDEFGHIJ");
        for(int i=0; i<10; i++){
            if (i==9) System.out.print((i+1)+" ");
            else System.out.print((i+1)+"  ");
            for (int j=0; j<10; j++){
                System.out.print(resultMap.charAt(j*10 + i));
            }
            System.out.println();
        }
    }

    private void printMyMap(){
        System.out.println("   ABCDEFGHIJ");
        for(int i=0; i<10; i++){
            if (i==9) System.out.print((i+1)+" ");
            else System.out.print((i+1)+"  ");
            for (int j=0; j<10; j++){
                System.out.print(myMap.charAt(j*10 + i));
            }
            System.out.println();
        }
    }


    private static int coordsToInt(String coordinates) {
        int col = coordinates.charAt(0)-'A';
        int row;
        if(coordinates.length()==3) row = 9;
        else row = Integer.parseInt(""+coordinates.charAt(1)) - 1;
        return col*10+row;
    }
    private static String intToCoords(int input) {
        int letterNumber = input/10;
        int row = input%10 + 1;
        char letter = (char)('A'+letterNumber);
        return letter+Integer.toString(row);
    }

    private static String setCharAtPosition(String string, int position, char c){
        return string.substring(0,position)+c+string.substring(position+1, string.length());
    }

    // function which takes map field (coordinate) as a parameter,
    // and checks if this part and all neighbour parts are destroyed i.e. whole ship is destroyed
    private static String mapCopy;
    private static boolean myWholeShipIsDestroyed(int shipPart, final String map){
        if (map.charAt(shipPart)=='.') return true;
        if (map.charAt(shipPart)=='~') return true;
        if (map.charAt(shipPart)=='#') return false;
        else{ // =='@'
            mapCopy = map;
            mapCopy = setCharAtPosition(mapCopy, shipPart, '.');
            return (shipPart%10==0 || myWholeShipIsDestroyed(shipPart-1,mapCopy))
                    && (shipPart%10==9 || myWholeShipIsDestroyed(shipPart+1, mapCopy))
                    && (shipPart>=90 || myWholeShipIsDestroyed(shipPart+10, mapCopy))
                    && (shipPart<=9 || myWholeShipIsDestroyed(shipPart-10, mapCopy));
        }
    }

    private void printBothMaps(){
        System.out.println("    your map               enemy map");
        System.out.println("   ABCDEFGHIJ             ABCDEFGHIJ");
        String enemyMap = "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@";
        for (int unknownField : unknownFields) enemyMap = setCharAtPosition(enemyMap,unknownField, '?');
        for (int waterField : waterFields) enemyMap = setCharAtPosition(enemyMap,waterField, '.');
        for(int i=0; i<10; i++){

            if (i==9) System.out.print((i+1)+" ");
            else System.out.print((i+1)+"  ");
            for (int j=0; j<10; j++){
                System.out.print(myMap.charAt(j*10 + i));
            }
            System.out.print("          ");
            if (i==9) System.out.print((i+1)+" ");
            else System.out.print((i+1)+"  ");
            for (int j=0; j<10; j++){
                System.out.print(enemyMap.charAt(j*10 + i));
            }
            System.out.println();
        }
    }

}

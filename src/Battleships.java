import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Battleships {

    private static Mode mode;
    private static String host;
    private static int port;
    private static String map;

    public static void main(String[] args) {
        // getting arguments
        for (int i=0; i<args.length; i=i+2){
            switch (args[i]){
                case "-mode":
                    if (args[i+1].equals("server")) mode = Mode.SERVER;
                    else if (args[i+1].equals("client")) mode = Mode.CLIENT;
                    else ExitHandler.exit("Bad input. usage: -mode [server|client]");
                    break;
                case "-host":
                    host = args[i+1];
                    break;
                case "-port":
                    port = Integer.parseInt(args[i+1]);
                    break;
                case "-map":
                    map = args[i+1];
                    break;
            }
        }
        ////////
        if(mode == Mode.SERVER){
            ServerManager serverManager = new ServerManager(port, map);
            serverManager.startGame();
        }
        else if(mode == Mode.CLIENT){
            ClientManager clientManager = new ClientManager(host, port, map);
            clientManager.startGame();
        }

    }

}

public class ExitHandler {

    public static void exit(int code, String msg){
        System.out.println(msg);
        System.exit(code);
    }

    public static void exit(){
        System.out.println("Exiting program.");
        System.exit(1);
    }

    public static void exit(String msg){
        System.out.println(msg);
        System.exit(1);
    }
}

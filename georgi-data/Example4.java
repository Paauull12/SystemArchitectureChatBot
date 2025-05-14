public class Example4 {
    public static void main(String[] args) {
        String result = "";
        for (int i = 0; i < 10000; i++) {
            result = result + "a";
        }
        System.out.println("Result string length: " + result.length());
    }
}
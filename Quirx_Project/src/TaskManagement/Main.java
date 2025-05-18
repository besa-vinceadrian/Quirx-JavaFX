package TaskManagement;

/**
 * Entry‑point for the Task‑Management program.  
 * <p>The flow is:
 * <ol>
 *   <li>User registers (or logs‑in if already registered).</li>
 *   <li>After successful login we transfer control to the CRUD task
 *       management menu implemented in {@link Crud_updated}.</li>
 * </ol>
 * <p>Both {@link Authentication} and {@link Crud_updated} are in the same
 * package so we can call their static methods directly.</p>
 */
public class Main {

    public static void main(String[] args) {
        java.util.Scanner sc = new java.util.Scanner(System.in);
        System.out.println("==============================");
        System.out.println("  Welcome to Task Manager CLI ");
        System.out.println("==============================\n");

        while (true) {
            System.out.println("1) Register\n2) Login\n0) Exit");
            System.out.print("Select option: ");
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1":
                    Authentication.registerUser(); // implements registration + OTP verification
                    break;
                case "2":
                    if (Authentication.loginUser()) {      // returns true on success
                        Crud_updated.main(new String[0]);        // hand‑off to CRUD UI
                    }
                    break;
                case "0":
                    System.out.println("Good‑bye!");
                    sc.close();
                    return;
                default:
                    System.out.println("Invalid option — try again.\n");
            }
        }
    }
}

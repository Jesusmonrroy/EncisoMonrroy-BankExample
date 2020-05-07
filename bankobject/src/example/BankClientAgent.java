/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package example;

/**
 *
 * @author Jesus Enrique Enciso Monrroy
 */


import java.io.*;
import java.util.Date;

import jade.core.*;
import jade.core.behaviours.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.*;
import jade.util.leap.*;

public class BankClientAgent extends Agent implements BankVocabulary {
// -------------------------------------------------------------------

   static final int WAIT = -1;
   static final int QUIT = 0;
   private int command = WAIT;
   private int cnt = 0;
   private AID server;
   private List accounts = new ArrayList();

   protected void setup() {

   // Set this agent main behaviour
      addBehaviour(new WaitUserCommand(this));
   }

   class WaitUserCommand extends OneShotBehaviour {
// ------------------------------------------------  Get the user command

      WaitUserCommand(Agent a) {
		 super(a);
		 command = WAIT;
	  }

      public void action() {

         command = getUserChoice();

         if (command == QUIT) {
		    System.out.println(getLocalName() + " is shutting down...Bye!");
		    doDelete();
		    System.exit(0);
		 }
		 if (command == NEW_ACCOUNT)
		    createAccount();

		 else if (command == DEPOSIT || command == WITHDRAWAL)
		    requestOperation();

		 else if (command == BALANCE || command == OPERATIONS)
			queryInformation();

		 else {
			System.out.println("Invalid choice!");
		    addBehaviour(new WaitUserCommand(myAgent));
		 }
	  }
   }


   void createAccount() {
// ----------------------  Process to the server agent the request
//                         to create a new account

	  CreateAccount ca = new CreateAccount();
	  ca.setName("acc" + cnt++);
	  sendMessage(ACLMessage.REQUEST, ca);
   }


   void requestOperation() {
// -------------------------  Process to the server agent the request
//                            to make an operation

      String accName = chooseAccount();
	  if (accName == null) {
		 addBehaviour(new WaitUserCommand(this));
		 return;
	  }
      Account acc = null;
	  if ((acc = findAccountByName(accName)) == null) {
		 System.out.println("Invalid account!");
		 addBehaviour(new WaitUserCommand(this));
		 return;
	  }
      String in = getUserInput("\nAmount: ");
	  if (in == null) {
		 System.out.println("Unable to process operation!");
		 return;
	  }
	  float amount = 0;
	  amount = Float.parseFloat(in);
	  MakeOperation mop = new MakeOperation();
	  mop.setAccountId(acc.getId());
	  mop.setType(command);
	  mop.setAmount(amount);
	  sendMessage(ACLMessage.REQUEST, mop);
   }

   void queryInformation() {
// -------------------------   Process to the server agent the request
//                             a query for information

      String accName = chooseAccount();
      Account acc = null;
	  if (accName == null || (acc = findAccountByName(accName)) == null) {
		 System.out.println("Invalid account!");
		 addBehaviour(new WaitUserCommand(this));
		 return;
	  }
	  String s = " charge applies for this operation. Continue?\ny/n: ";
	  s = command==BALANCE ? "\n\n\t$" + BAL_CHARGE + s:
	                         "\n\n\t$" + OPER_CHARGE + s;
	  String in = getUserInput(s);
	  if (in.equals("y")) {
	     Information info = new Information();
	     info.setType(command);
	     info.setAccountId(acc.getId());
	     sendMessage(ACLMessage.QUERY_REF, info);
	     return;
	  }
	  if (in.equals("n")) System.out.println("\nOperation aborted!");
	  else System.out.println("\nInvalid choice!");
	  addBehaviour(new WaitUserCommand(this));
   }


   class WaitServerResponse extends ParallelBehaviour {
// ----------------------------------------------------  launch a SimpleBehaviour to receive
//                                                       servers response and a WakerBehaviour
//                                                       to terminate the waiting if there is
//                                                       no response from the server
      WaitServerResponse(Agent a) {

	     super(a, 1);

	     addSubBehaviour(new ReceiveResponse(myAgent));

	     addSubBehaviour(new WakerBehaviour(myAgent, 5000) {

		    protected void handleElapsedTimeout() {
			   System.out.println("\n\tNo response from server. Please, try later!");
			   addBehaviour(new WaitUserCommand(myAgent));
			}
		 });
	  }
   }


   class ReceiveResponse extends SimpleBehaviour {
// -----------------------------------------------  // Receive and handle server responses

      private boolean finished = false;

      ReceiveResponse(Agent a) {
	     super(a);
	  }

	  public void action() {

	     ACLMessage msg = receive(MessageTemplate.MatchSender(server));
	     if (msg == null) { block(); return; }

		 if (msg.getPerformative() == ACLMessage.NOT_UNDERSTOOD){
		    System.out.println("\n\tResponse from server: NOT UNDERSTOOD!");
		 }
		 else {
	        try {
		       Object content = msg.getContentObject();

		       if (content instanceof Problem) {
		          System.out.println("\n\n\tResponse from server: " +
		                            ((Problem)content).getMsg().toUpperCase());
			   }

               //-------------------- Create a new accont -------------------------

			   else if (command == NEW_ACCOUNT) {

		          Account acc = (Account) content;
		          accounts.add(acc);
		          System.out.println("\n\n\tResponse from server:\nAccount [" +
		                             acc.toString() + "] created!");
		       }
		       //---------------- Make deposit or withdrawal -----------------------

		       else if (command == DEPOSIT || command == WITHDRAWAL) {

		          if (msg.getPerformative() == ACLMessage.INFORM){
			         if (content instanceof Account) {
			            Account acc = (Account)content;
		                System.out.println("\n\n\tResponse from server:\nAccount [" +
		                                   acc.toString() + "]");
			         }
			         else System.out.println("\nUnexpected msg content from server!");
		          }
		          else  System.out.println("\nUnexpected msg from server!");
	           }
	           //---------------- Get Balance or operations list --------------------

		       else if (command == BALANCE || command == OPERATIONS) {

	              if (msg.getPerformative() == ACLMessage.INFORM){
			         if (content instanceof Account) {
			            Account acc = (Account)content;
		                System.out.println("\n\n\tResponse from server:\nAccount [" +
		                                   acc.toString() + "]");
			         }
			         else if (content instanceof OperationList) {
			            List list = ((OperationList) content).getOperations();
			            String accId = ((OperationList) content).getAccountId();
			            Account acc = findAccountById(accId);
			            String s = "\n\n\tLIST OF OPERATIONS: Account [" + acc.getName() +
			                       " # " + acc.getId() + "]\n\tDate\tType\tAmount\tBalance";
			            for (Iterator it = list.iterator(); it.hasNext();)
                           s += ((Operation)it.next()).toString();
			            System.out.println(s);
			         }
			         else System.out.println("\nUnexpected msg content from server!");
			      }
		          else  System.out.println("\nUnexpected msg from server!");
		       }
		    }
		    catch (Exception e) { e.printStackTrace(); }
		 }
	     finished = true;
	  }

	  public boolean done() { return finished; }

	  public int onEnd() {
	     addBehaviour(new WaitUserCommand(myAgent));
	     return 0;
	  }
   }


   void lookupServer() {
// ---------------------  Search in the DF to retrieve the server AID

	  ServiceDescription sd = new ServiceDescription();
      sd.setType(SERVER_AGENT);
	  DFAgentDescription dfd = new DFAgentDescription();
      dfd.addServices(sd);
      try {
	     DFAgentDescription[] dfds = DFService.search(this, dfd);
		 if (dfds.length > 0 ) {
		    server = dfds[0].getName();
            System.out.println("Localized server");
		 }
		 else  System.out.println("Couldn't localize server!");
      }
      catch (Exception ex) {
	     ex.printStackTrace();
         System.out.println("Failed searching int the DF!");
	  }
   }


//--------------------------- Utility methods ----------------------------//


   Account findAccountByName(String name) {
// ----------------------------------------
      for (Iterator it = accounts.iterator(); it.hasNext();) {
         Account acc = (Account)it.next();
         if (acc.getName().equals(name))
			return acc;
	  }
      return null;
   }

   Account findAccountById(String id) {
// ----------------------------------------
      for (Iterator it = accounts.iterator(); it.hasNext();) {
         Account acc = (Account)it.next();
         if (acc.getId().equals(id))
			return acc;
	  }
      return null;
   }

   String chooseAccount() {
// ------------------------

	  if (accounts.isEmpty()) {
		 System.out.println("No account currently available!");
		 return null;
	  }
	  // Display account list
	  String out = "\n\t<<----- AVAILABLE ACCOUNT(S) ----->>\n";
	  for (Iterator it = accounts.iterator(); it.hasNext();)
	     out += "\n\t" + ((Account)it.next()).getName();

      return getUserInput(out + "\n\nAccount: ");
   }

   int getUserChoice() {
// ---------------------

      System.out.print("\n\t<<---- Bank Example Objects ---->>" +
                       "\n\t<<---- SAVINGS ACOUNT - MENU ---->>" +
                       "\n\n\t0. Terminate program" +
                       "\n\t1. Create a new account\n\t2. Make a deposit" +
                       "\n\t3. Make a withdrawal\n\t4. Get account balance" +
                       "\n\t5. Get list of operations\n> ");
      try {
		 BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
         String in = buf.readLine();
         return Integer.parseInt(in);
	  }
	  catch (Exception ex) { ex.printStackTrace(); }
	  return 0;
   }

   int getUserChoice(String msg) {
// -------------------------------

      System.out.print(msg);
      try {
		 BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
         String in = buf.readLine();
         return Integer.parseInt(in);
	  }
	  catch (Exception ex) { ex.printStackTrace(); }
	  return -1;
   }

   String getUserInput(String msg) {
// ---------------------------------

      System.out.print(msg);
      try {
		 BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
         return buf.readLine();
	  }
	  catch (Exception ex) { ex.printStackTrace(); }
	  return null;
   }

   void sendMessage(int performative, Object content) {
// ----------------------------------------------------

	  if (server == null) lookupServer();
	  if (server == null) {
		 System.out.println("Unable to localize the server! Operation aborted!");
		 return;
	  }
      ACLMessage msg = new ACLMessage(performative);
	  try {
		 msg.setContentObject((java.io.Serializable)content);
		 msg.addReceiver(server);
		 System.out.println("Contacting server... Please wait!");
		 send(msg);
	     addBehaviour(new WaitServerResponse(this));
	  }
	  catch (Exception ex) { ex.printStackTrace(); }
   }
}
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
/*******************************************************************************
*
*  TableauDeBord:
*  -------------
*  This class defines the Gui for the BankAgentGui. It shows how to build an
*  agent with an integrated GUI.
*
*  Version 1.0 - July 2003.
*  Authors: Ambroise Ncho, under the supervision of Professeur Jean Vaucher.
*
*  Departement d'Informatique et de Recherche Operationnelle (DIRO).
*  Universite de Montreal.
*
********************************************************************************/

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.border.*;

import jade.core.*;
import jade.gui.*;

import ontologies.*;


class BankAgentGui extends JFrame implements ActionListener, BankVocabulary {
// --------------------------------------------------------------------------

   Object[] actions = {"","New Account","Deposit","Withdrawal",
                       "Balance","Operations"};
   String columns[] = {"Date","Operation","Debit","Credit","Balance"};
   final static int IN_PROCESS = 0;
   final static int WAIT_CONFIRM = 1;
   final static int IN_LINE = 2;
   private int status = IN_PROCESS;
   private java.util.List accounts = new ArrayList();
   private JTextField msg, input, acInfo;
   private JComboBox menu;
   private JList acList;
   private JTable opTable;
   private JButton ok, cancel, quit;
   private BankClientAgent myAgent;


   public BankAgentGui(BankClientAgent a) {
// ----------------------------------------  Constructor

      myAgent = a;      // Reference to class BankClientAgent

      setTitle("Bank Client Agent - " + myAgent.getLocalName());

      JPanel base = new JPanel();
      base.setBorder(new EmptyBorder(15,15,15,15));
      base.setLayout(new BorderLayout(10,10));
      getContentPane().add(base);

      JPanel panel = new JPanel();
      base.add(panel, BorderLayout.WEST);
      panel.setLayout(new BorderLayout(0,16));
      JPanel pane = new JPanel();
      pane.setLayout(new BorderLayout(0,0));
      pane.add(new JLabel("Message"), BorderLayout.NORTH);
      pane.add(msg = new JTextField("Select an action", 15));
      msg.setEditable(false);
      msg.setBackground(Color.black);
      msg.setForeground(Color.white);
      msg.setFont(new Font("Arial", Font.BOLD, 12));
      msg.setHorizontalAlignment(JTextField.CENTER);
      panel.add(pane, BorderLayout.NORTH);
      pane = new JPanel();
      pane.setLayout(new BorderLayout(5,0));

      JPanel p = new JPanel();
      p.setLayout(new BorderLayout(0,0));
      p.add(new JLabel("Accounts"), BorderLayout.NORTH);
      acList = new JList();
      acList.setVisibleRowCount(5);
      acList.setFixedCellHeight(18);
      acList.setFont(new Font("Arial", Font.PLAIN, 12));
      acList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
      JScrollPane scroll = new JScrollPane(acList);
      scroll.setPreferredSize(new Dimension(160,60));
      p.add(scroll, BorderLayout.SOUTH);
      pane.add(p, BorderLayout.WEST);
      p = new JPanel();
      p.setBorder(new EmptyBorder(0,0,35,0));
      p.setLayout(new BorderLayout(0,0));
      p.add(new JLabel("Menu"), BorderLayout.NORTH);
      p.add(menu = new JComboBox(actions), BorderLayout.SOUTH);
      pane.add(p, BorderLayout.EAST);
      panel.add(pane, BorderLayout.CENTER);
      pane = new JPanel();
      pane.setLayout(new BorderLayout(0,0));
      JPanel p0 = new JPanel();
      p0.setLayout(new BorderLayout(0,3));
      p = new JPanel();
      p.setLayout(new BorderLayout(10,0));
      p.add(new JLabel("Operations"), BorderLayout.WEST);
      p.add(acInfo = new JTextField(25), BorderLayout.EAST);
      acInfo.setEditable(false);
      acInfo.setFont(new Font("Arial", Font.PLAIN, 11));
      msg.setHorizontalAlignment(JTextField.CENTER);
      p0.add(p, BorderLayout.NORTH);
      Object obj[][] = new Object[0][columns.length];
      TableModel model = new TableDataModel(obj, columns);
      opTable = new JTable(model);
      opTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      opTable.setPreferredScrollableViewportSize(new Dimension(320,100));
      opTable.setFont(new Font("Arial", Font.PLAIN, 11));
      p0.add(new JScrollPane(opTable), BorderLayout.SOUTH);
      pane.add(p0, BorderLayout.SOUTH);
      panel.add(pane, BorderLayout.SOUTH);

      panel = new JPanel();
      base.add(panel, BorderLayout.EAST);
      panel.setLayout(new BorderLayout(0,10));
      pane = new JPanel();
      pane.setLayout(new BorderLayout(0,0));
      pane.add(new JLabel("Input"), BorderLayout.NORTH);
      pane.add(input = new JTextField(8));
      panel.add(pane, BorderLayout.NORTH);
      pane = new JPanel();
      panel.add(pane, BorderLayout.SOUTH);
      pane.setBorder(new EmptyBorder(0,0,130,0));
      pane.setLayout(new GridLayout(3,1,0,5));
      pane.add(ok = new JButton("OK"));
      ok.setToolTipText("Submit operation");
      ok.addActionListener(this);
      pane.add(cancel = new JButton("Cancel"));
      cancel.setToolTipText("Submit operation");
      cancel.setEnabled(false);
      cancel.addActionListener(this);
      pane.add(quit = new JButton("QUIT"));
      quit.setToolTipText("Stop agent and exit");
      quit.addActionListener(this);

      addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
            shutDown();
         }
      });

      setSize(470, 350);
      setResizable(false);
      Rectangle r = getGraphicsConfiguration().getBounds();
      setLocation(r.x + (r.width - getWidth())/2,
                  r.y + (r.height - getHeight())/2);
   }

   public void actionPerformed(ActionEvent ae) {
// ---------------------------------------------

      if (ae.getSource() == quit) {
         shutDown();
      }
      else if (ae.getSource() == ok) {

         if (menu.getSelectedItem().equals(actions[0])) {
            alertInfo("Select an action in the menu");
         }
         else if (menu.getSelectedItem().equals(actions[1])) {
            GuiEvent ge = new GuiEvent(this, NEW_ACCOUNT);
            myAgent.postGuiEvent(ge);
         }
         else if (menu.getSelectedItem().equals(actions[2]) ||
                  menu.getSelectedItem().equals(actions[3])) {
            if (accounts.isEmpty()) {
               alertInfo("No account currently available");
               return;
            }
            if (acList.getSelectedIndex() == -1) {
               alertInfo("Select an account");
               return;
            }
            if (input.getText().length() == 0) {
               alertInfo("Enter an amount");
               input.requestFocus();
               return;
            }
            try {
               float amount = Float.parseFloat(input.getText());
               int type = menu.getSelectedItem().equals(actions[2])?
                          DEPOSIT : WITHDRAWAL;
               GuiEvent ge = new GuiEvent(this, type);
               ge.addParameter(accounts.get(acList.getSelectedIndex()));
               ge.addParameter(new Float(amount));
               myAgent.postGuiEvent(ge);
            }
            catch (Exception ex) { alertInfo("Invalid input. Operation aborted!"); }
         }
         else if (menu.getSelectedItem().equals(actions[4]) ||
                  menu.getSelectedItem().equals(actions[5]) &&
                  status != IN_LINE) {
            if (accounts.isEmpty()) {
               alertInfo("No account currently available");
               return;
            }
            if (acList.getSelectedIndex() == -1) {
               alertInfo("Select an account");
               return;
            }
            int type = menu.getSelectedItem().equals(actions[4])?
                       BALANCE : OPERATIONS;
            if (status == IN_PROCESS) {
               if (type == BALANCE)
                  alertInfo("$1.25 charge for this operation. Continue?");
               else alertInfo("$2.50 charge for this operation. Continue?");
               status = WAIT_CONFIRM;
               cancel.setEnabled(true);
               return;
            }
            if (status == WAIT_CONFIRM) {
               status = IN_LINE;
               cancel.setEnabled(false);
               GuiEvent ge = new GuiEvent(this, type);
               ge.addParameter(accounts.get(acList.getSelectedIndex()));
               myAgent.postGuiEvent(ge);
            }
         }
      }
      else if (ae.getSource() == cancel && status != IN_LINE) {
         status = IN_PROCESS;
         cancel.setEnabled(false);
         msg.setText("Operation canceled!");
      }
   }

   void shutDown() {
// -----------------  Control the closing of this gui

      int rep = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?",
                                              myAgent.getLocalName(),
                                              JOptionPane.YES_NO_CANCEL_OPTION);
      if (rep == JOptionPane.YES_OPTION) {
         GuiEvent ge = new GuiEvent(this, myAgent.QUIT);
         myAgent.postGuiEvent(ge);
      }
   }

   void alertInfo(String s) {
// --------------------------

      Toolkit.getDefaultToolkit().beep();
      msg.setText(s);
   }

   public void alertResponse(Object o) {
// -------------------------------------

      String s = "";
      if (o instanceof Problem) {  // This a failure response from server
         s = ((Problem)o).getMsg();
      }
      else if (o instanceof Account) {
         Account acc = (Account)o;
         if (updateAccount(acc)) {  //  <-- Existing account just updated
            s = acc.getName() + " - Balance: " + acc.getBalance();
         }
         else {   //  <-- This is a new account just added
            java.util.Vector v = getAccountList();
            acList.setListData(v);
            acList.setSelectedIndex(accounts.size()-1);
            s = acc.getName() + " created!";
         }
      }
      else if (o instanceof jade.util.leap.List) {  //  <-- This is a list of operations
         displayOperations((jade.util.leap.List)o);
      }
      else if (o instanceof String) {
         s = (String)o;
      }
      msg.setText(s);
   }

   boolean updateAccount(Account acc) {
// ------------------------------------

      for (Iterator it = accounts.iterator(); it.hasNext();) {
         Account a = (Account)it.next();
         if (a.getId().equals(acc.getId())) {
            int i = accounts.indexOf(a);
            accounts.set(i, acc);  //  <-- Account already exists: update it
            return true;
         }
      }
      accounts.add(acc);  //  <-- New account: add to the list of accounts
      return false;
   }

   java.util.Vector getAccountList() {
// -----------------------------------

      java.util.Vector v = new java.util.Vector();
      for (int i = 0; i < accounts.size(); i++) {
         Account acc = (Account)accounts.get(i);
         v.add((i+1) + ". " + acc.getName() + "   #" + acc.getId());
      }
      return v;
   }

   void displayOperations(jade.util.leap.List list) {
// --------------------------------------------------

      Account acc = (Account)accounts.get(acList.getSelectedIndex());
      acInfo.setText(acc.getName() + "     #" + acc.getId());
      Object[][] data = new Object[list.size()][columns.length];
      for (int i = 0; i < list.size(); i++) {
         Operation op = (Operation)list.get(i);
         data[i][0] = op.getDate();
         data[i][1] = op.getName();
         if (op.getType() == WITHDRAWAL || op.getType() == ADMIN)
            data[i][2] = new Float(op.getAmount());
         else data[i][3] = new Float(op.getAmount());
         data[i][4] = new Float(op.getBalance());
      }
      TableDataModel model = (TableDataModel)opTable.getModel();
      model.setData(data);
      opTable.setModel(model);
      opTable.updateUI();
   }

   public void resetStatus() {
// ---------------------------

      status = IN_PROCESS;
   }
}


// =========================== External class ============================//

/* TableDataModel:
*  --------------
*  External class for the definition of the tables data model, used to
*  control the display of data within the different tables
**/
   class TableDataModel extends AbstractTableModel {
// -------------------------------------------------

   private String[] columns;
   private Object[][] data;

   public TableDataModel(Object[][] data, String[] columns) {
// ----------------------------------------------------------  Constructor
      this.data = data;
      this.columns = columns;
   }

   public int getColumnCount() {
// -----------------------------  Return the number of columns in the table
      return columns.length;
   }

   public int getRowCount() {
// --------------------------  Return the number of rows in the table
      return data.length;
   }

   public String getColumnName(int col) {
// --------------------------------------  Return the name of a column
      return columns[col];
   }

   public Object getValueAt(int row, int col) {
// --------------------------------------------  Return the value at a specific
//                                               row and column
      if ( data.length == 0 ) return null;
      return data[row][col];
   }

   public Class getColumnClass(int col) {
// --------------------------------------  Return the class of the values held
//                                         by a column
      Object o = getValueAt(0, col);
      if (o == null) return columns[col].getClass();
      return getValueAt(0, col).getClass();
   }

   public void setValueAt(Object value, int row, int col){
// ------------------------------------------------------  Set the value at a specific
//                                                         row and column
      data[row][col] = value;
   }

   public void setData(Object[][] data){
// ------------------------------------  Update the entire data in the table
      this.data = data;
   }

   Object[][] getData(){
// ---------------------  Return the entire data of the table
      return data;
   }
}// end TableDataModel

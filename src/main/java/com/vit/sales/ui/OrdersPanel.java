package com.vit.sales.ui;

import com.vit.sales.db.DatabaseManager;
import javax.swing.*;import javax.swing.table.DefaultTableModel;import java.awt.*;import java.math.BigDecimal;import java.sql.*;

/** Transactional order creation demo: one product line per order, extensible to multi-line orders. */
public final class OrdersPanel extends JPanel {
 private final DatabaseManager db; private final String role; private final JTable table=new JTable();
 public OrdersPanel(DatabaseManager db,String role){this.db=db;this.role=role;setLayout(new BorderLayout(8,8));setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
  JPanel top=new JPanel(new FlowLayout(FlowLayout.LEFT));JButton refresh=new JButton("Refresh"),create=new JButton("Create order");refresh.addActionListener(e->load());create.addActionListener(e->createOrder());top.add(create);top.add(refresh);add(top,BorderLayout.NORTH);table.setAutoCreateRowSorter(true);add(new JScrollPane(table),BorderLayout.CENTER);load();}
 private void load(){String sql="SELECT o.order_id,c.customer_name,o.order_date,o.order_status,COALESCE(SUM(d.quantity*d.unit_price),0) total FROM Orders o JOIN Customer c ON c.customer_id=o.customer_id LEFT JOIN Order_Details d ON d.order_id=o.order_id GROUP BY o.order_id,c.customer_name,o.order_date,o.order_status ORDER BY o.order_id DESC";try(Connection c=db.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){DefaultTableModel m=new DefaultTableModel();ResultSetMetaData md=r.getMetaData();for(int i=1;i<=md.getColumnCount();i++)m.addColumn(md.getColumnLabel(i));while(r.next()){Object[] a=new Object[md.getColumnCount()];for(int i=1;i<=a.length;i++)a[i-1]=r.getObject(i);m.addRow(a);}table.setModel(m);}catch(SQLException e){error(e);}}
 private void createOrder(){try(Connection c=db.getConnection()){
  JComboBox<String> customers=new JComboBox<>();try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT customer_id,customer_name FROM Customer ORDER BY customer_name")){while(r.next())customers.addItem(r.getInt(1)+" - "+r.getString(2));}
  JComboBox<String> products=new JComboBox<>();try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT product_id,product_name,price,stock_quantity FROM Product WHERE stock_quantity>0 ORDER BY product_name")){while(r.next())products.addItem(r.getInt(1)+" - "+r.getString(2)+" | ₹"+r.getBigDecimal(3)+" | stock "+r.getInt(4));}
  if(customers.getItemCount()==0||products.getItemCount()==0){JOptionPane.showMessageDialog(this,"Add at least one customer and one in-stock product first.");return;}
  JTextField qty=new JTextField("1",6);JComboBox<String> modes=new JComboBox<>(new String[]{"Cash","Card","UPI","Net Banking"});JPanel form=new JPanel(new GridLayout(0,2,8,8));form.add(new JLabel("Customer"));form.add(customers);form.add(new JLabel("Product"));form.add(products);form.add(new JLabel("Quantity"));form.add(qty);form.add(new JLabel("Payment mode"));form.add(modes);
  if(JOptionPane.showConfirmDialog(this,form,"Create order",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
  int customerId=Integer.parseInt(((String)customers.getSelectedItem()).split(" - ")[0]);String p=(String)products.getSelectedItem();int productId=Integer.parseInt(p.split(" - ")[0]);int quantity=Integer.parseInt(qty.getText().trim());if(quantity<1)throw new IllegalArgumentException("Quantity must be positive.");
  c.setAutoCommit(false);try{
   BigDecimal price;int stock;try(PreparedStatement ps=c.prepareStatement("SELECT price,stock_quantity FROM Product WHERE product_id=? FOR UPDATE")){ps.setInt(1,productId);try(ResultSet r=ps.executeQuery()){if(!r.next())throw new SQLException("Product no longer exists.");price=r.getBigDecimal(1);stock=r.getInt(2);}}
   if(quantity>stock)throw new IllegalArgumentException("Insufficient stock. Available: "+stock);
   int orderId;try(PreparedStatement ps=c.prepareStatement("INSERT INTO Orders(customer_id,order_status) VALUES (?, 'Pending')",Statement.RETURN_GENERATED_KEYS)){ps.setInt(1,customerId);ps.executeUpdate();try(ResultSet k=ps.getGeneratedKeys()){k.next();orderId=k.getInt(1);}}
   try(PreparedStatement ps=c.prepareStatement("INSERT INTO Order_Details(order_id,product_id,quantity,unit_price) VALUES(?,?,?,?)")){ps.setInt(1,orderId);ps.setInt(2,productId);ps.setInt(3,quantity);ps.setBigDecimal(4,price);ps.executeUpdate();}
   try(PreparedStatement ps=c.prepareStatement("UPDATE Product SET stock_quantity=stock_quantity-? WHERE product_id=? AND stock_quantity>=?")){ps.setInt(1,quantity);ps.setInt(2,productId);ps.setInt(3,quantity);if(ps.executeUpdate()!=1)throw new SQLException("Stock changed; retry the order.");}
   BigDecimal total=price.multiply(BigDecimal.valueOf(quantity));try(PreparedStatement ps=c.prepareStatement("INSERT INTO Payment(order_id,amount,payment_mode) VALUES(?,?,?)")){ps.setInt(1,orderId);ps.setBigDecimal(2,total);ps.setString(3,(String)modes.getSelectedItem());ps.executeUpdate();}
   c.commit();JOptionPane.showMessageDialog(this,"Order #"+orderId+" created. Total: ₹"+total+". Stock and payment updated.");load();
  }catch(Exception ex){c.rollback();throw ex;}finally{c.setAutoCommit(true);}
 }catch(Exception e){JOptionPane.showMessageDialog(this,e.getMessage(),"Could not create order",JOptionPane.ERROR_MESSAGE);}}
 private void error(Exception e){JOptionPane.showMessageDialog(this,e.getMessage(),"Database error",JOptionPane.ERROR_MESSAGE);}
}

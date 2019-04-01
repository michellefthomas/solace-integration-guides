package com.solace.sample;

import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.transaction.UserTransaction;

@Stateless(name = "XAProducerBMTSB")
@TransactionManagement(value=TransactionManagementType.BEAN)

public class XAProducerBMTSB implements Producer, ProducerLocal {
    @Resource(name = "myCF")
    ConnectionFactory myCF;

    @Resource(name = "myReplyQueue")
    Queue myReplyQueue;

    @Resource
    SessionContext sessionContext;
    
    public XAProducerBMTSB() {
    }

    @Override
    public void sendMessage() throws JMSException {

        System.out.println("Sending reply message");
        Connection conn = null;
        Session session = null;
        MessageProducer prod = null;
        UserTransaction ux = sessionContext.getUserTransaction();

        try {
            ux.begin();
            conn = myCF.createConnection();
            session = conn.createSession(true, Session.AUTO_ACKNOWLEDGE);
            prod = session.createProducer(myReplyQueue);
            ObjectMessage msg = session.createObjectMessage();
            msg.setObject("Hello world!");
            prod.send(msg, DeliveryMode.PERSISTENT, 0, 0);
            ux.commit();
        } catch (Exception e) {
            e.printStackTrace();
            try {
               ux.rollback();
            } catch (Exception ex) {
               throw new EJBException(
                "rollback failed: " + ex.getMessage(), ex);
            }
        } finally {
            if (prod != null)
          prod.close();
            if (session != null)
          session.close();
            if (conn != null)
          conn.close();
        }
    }
}

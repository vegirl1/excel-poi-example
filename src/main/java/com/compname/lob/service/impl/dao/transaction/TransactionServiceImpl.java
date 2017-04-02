package com.compname.lob.service.impl.dao.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;


/**
 * TransactionService Implementation; will execute TransactionCommand commands and will commit/rollback the result
 * 
 * see http://docs.spring.io/spring/docs/current/spring-framework-reference/html/transaction.html
 * 
 * @author vegirl1
 * @since May 27, 2015
 * @version $Revision$
 */
public class TransactionServiceImpl implements TransactionService {

    private static final Logger       LOG = LoggerFactory.getLogger(TransactionServiceImpl.class);

    // single TransactionTemplate shared amongst all methods in this instance
    private final TransactionTemplate transactionTemplate;

    /**
     * Class constructor.
     * 
     */
    @Autowired
    public TransactionServiceImpl(PlatformTransactionManager transactionManager, TransactionDefinition transactionDefinition) {
        Assert.notNull(transactionManager, "The 'transactionManager' wasn't supplied.");
        Assert.notNull(transactionDefinition, "The 'transactionDefinition' wasn't supplied.");

        this.transactionTemplate = new TransactionTemplate(transactionManager, transactionDefinition);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.impl.dao.transaction.TransactionService#executeTransaction(com.compname.lob.service.impl.dao.transaction.TransactionCommand)
     */
    @Override
    public void executeTransaction(final TransactionCommand command) throws ServiceException {

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    command.execute();
                } catch (Exception ex) {
                    status.setRollbackOnly();
                    LOG.error("TransactionServiceImpl.executeTransaction() failed .Error message :{} ", ex.getLocalizedMessage());
                    throw new RuntimeException(ex);
                }
            }
        });

    }

}

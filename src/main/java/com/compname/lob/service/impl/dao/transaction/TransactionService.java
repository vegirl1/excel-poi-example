package com.compname.lob.service.impl.dao.transaction;


/**
 * TransactionService interface
 * 
 * @author vegirl1
 * @since May 27, 2015
 * @version $Revision$
 */
public interface TransactionService {

    void executeTransaction(final TransactionCommand command) throws ServiceException;

}

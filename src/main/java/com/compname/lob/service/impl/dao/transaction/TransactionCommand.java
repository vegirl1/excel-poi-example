package com.compname.lob.service.impl.dao.transaction;

/**
 * TransactionCommand interface; to be implemented when calling TransactionService
 * 
 * @author vegirl1
 * @since May 27, 2015
 * @version $Revision$
 */
public interface TransactionCommand {
    void execute();
}

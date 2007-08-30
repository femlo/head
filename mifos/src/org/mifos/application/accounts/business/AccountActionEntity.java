package org.mifos.application.accounts.business;

import org.mifos.application.accounts.util.helpers.AccountActionTypes;
import org.mifos.application.master.business.MasterDataEntity;

/**
 * Also see {@link AccountActionTypes}.
 */
public class AccountActionEntity extends MasterDataEntity {
	
	public AccountActionEntity() {
		super();
	}

	public AccountActionEntity(AccountActionTypes myEnum) {
		super(myEnum.getValue());
	}

	public AccountActionTypes asEnum() {
		return AccountActionTypes.fromInt(getId());
	}

    public String toString() {
        return AccountActionTypes.fromInt(getId()).toString();
    }
}

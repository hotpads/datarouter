package com.hotpads.datarouter.storage.key.entity.base;

import com.hotpads.datarouter.storage.field.BaseFieldSet;
import com.hotpads.datarouter.storage.key.entity.EntityKey;

@SuppressWarnings("serial") 
public abstract class BaseEntityKey<EK extends EntityKey<EK>>
extends BaseFieldSet<EK>
implements EntityKey<EK>
{

}

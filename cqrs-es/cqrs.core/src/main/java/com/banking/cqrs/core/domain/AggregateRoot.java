package com.banking.cqrs.core.domain;

import com.banking.cqrs.core.evets.BaseEvent;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public abstract class AggregateRoot {
    protected String id;
    private int version = -1;

    private final List<BaseEvent> changes =  new ArrayList<>();

    private final Logger loggeer = Logger.getLogger(AggregateRoot.class.getName());

    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id =  id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public List<BaseEvent> getUncommitedChanges() {
        return changes;
    }

    public void markChangesAsCommited(){
        this.changes.clear();
    }

    protected void applyChange(BaseEvent event, Boolean isNewEvent){
        try {
            var method = getClass().getDeclaredMethod("apply",event.getClass());
            method.setAccessible(true);
            method.invoke(this,event);
        }catch (NoSuchMethodException e){
            loggeer.warning(MessageFormat.format("El metodo apply no fue encontrado para {0}", event.getClass().getName()));
        }catch (Exception e){
            loggeer.severe("Errores aplicando el evento al aggregate: " + e);
        }finally {
            if (isNewEvent)
                changes.add(event);
        }
    }

    public void raiseEvent(BaseEvent event){
        applyChange(event,true);
    }

    public void replayEvents(Iterable<BaseEvent> events){
        events.forEach(event -> applyChange(event,false));
    }
}

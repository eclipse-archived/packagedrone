package org.eclipse.packagedrone.repo.trigger;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;

public interface TriggerInstance
{
    public @NonNull Optional<Trigger> getTrigger ();
}

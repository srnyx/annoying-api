package xyz.srnyx.annoyingapi.message;

import org.jetbrains.annotations.NotNull;
import xyz.srnyx.annoyingapi.parents.Registrable;


public abstract class MessagesProvider extends Registrable {
    public abstract void setMessages(@NotNull AnnoyingMessages messages);

    @NotNull
    public abstract AnnoyingMessages getMessages();
}

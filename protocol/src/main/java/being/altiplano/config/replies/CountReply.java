package being.altiplano.config.replies;

import being.altiplano.config.Command;
import being.altiplano.config.Reply;

import java.nio.ByteBuffer;

/**
 * @see being.altiplano.config.commands.CountCommand
 */
public class CountReply implements Reply {
    private final int count;

    public CountReply(int count) {
        this.count = count;
    }

    public CountReply(byte[] data) {
        this.count = ByteBuffer.wrap(data).getInt();
    }

    public int code() {
        return Command.COUNT;
    }

    public int getCount() {
        return count;
    }

    public byte[] toBytes() {
        return ByteBuffer.allocate(4).putInt(count).array();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " " + count;
    }
}

package module.decode.p25.message.tsbk.osp.voice;

import module.decode.p25.message.tsbk.UnitChannelGrant;
import module.decode.p25.reference.DataUnitID;
import module.decode.p25.reference.Opcode;
import alias.AliasList;
import bits.BinaryMessage;

public class UnitToUnitVoiceChannelGrantUpdate extends UnitChannelGrant
{
    public UnitToUnitVoiceChannelGrantUpdate( BinaryMessage message, 
            DataUnitID duid, AliasList aliasList )
    {
        super( message, duid, aliasList );
    }

    @Override
    public String getEventType()
    {
        return Opcode.UNIT_TO_UNIT_VOICE_CHANNEL_GRANT_UPDATE.getDescription();
    }
}
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.turan.netty.codec;

import com.turan.example.protocol.message.dev.D_ByteData;
import com.turan.example.protocol.structure.T808Message;
import com.turan.example.protocol.util.BufferUtil;
import com.turan.example.protocol.util.MessageUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class NettyDecoder extends ByteToMessageDecoder
{

    private final static int BYTE_STREAM_LENGTH_FIELD_OFFSET = 58;
    private final static int BYTE_STREAM_LENGTH_FIELD_LENGTH = 4;

    private static final int MSG_BODY_LENGTH_BITS = 0x1FF;
    private static final int MSG_MARK_LENGTH = 1;
    private static final int MSG_VALID_LENGTH = 1;
    private static final int MSG_HEADER_EXCLUDE_PKG_INFO_LENGTH = 12;
    private static final int MSG_HEADER_PKG_INFO_LENGTH = 4;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        try
        {
            Object result = null;

            int msgMark = in.getUnsignedByte(in.readerIndex());
            long magic = in.getUnsignedInt(in.readerIndex());

            if (msgMark == T808Message.MSG_MARK)
            {
                result = decodeInstruction(in);
            } else if (magic == D_ByteData.FRAME_MARK)
            {
                result = decodeByteStream(in);
            } else
            {
                log.warn("msg not start with [{}] or [{}]",Integer.toHexString(T808Message.MSG_MARK),Integer.toHexString(D_ByteData.FRAME_MARK));
                in.skipBytes(1); // 既不是信令数据又不是码流数据,跳过
            }

            if (result != null)
            {
                out.add(result);
            }

        } catch (Exception e)
        {
            log.error("decode exception", e);
            ctx.close();
        }
    }

    private Object decodeInstruction(ByteBuf in)
    {
        // 读取的数据小于5个字节,不能读取消息体的长度
        if(in.readableBytes() < 5)
        {
            return null;
        }

        // 读取消息的长度,跳过消息id和标志位
        int bodyAttr = in.getUnsignedShort(in.readerIndex() + 3);
        int bodyLength = bodyLength(bodyAttr) ;
        boolean ifDivide = MessageUtil.ifPkgDivide(bodyAttr);

        int msgLength = MSG_MARK_LENGTH + MSG_HEADER_EXCLUDE_PKG_INFO_LENGTH +
                (ifDivide ? MSG_HEADER_PKG_INFO_LENGTH : 0)
                + bodyLength
                + MSG_VALID_LENGTH + MSG_MARK_LENGTH;

        if(in.readableBytes() < msgLength)
        {
            return null;
        }

        ByteBuf msgBuf = in.readBytes(msgLength);
        if(msgBuf.getUnsignedByte(msgBuf.readableBytes() - 1) != T808Message.MSG_MARK)
        {
            log.warn("not valid msg");
            return null;
        }

        return decodeT808Msg(msgBuf);
    }

    private Object decodeByteStream(ByteBuf in)
    {
        if (in.readableBytes() < BYTE_STREAM_LENGTH_FIELD_OFFSET + BYTE_STREAM_LENGTH_FIELD_LENGTH)
        {
            return null;
        }

        long bodyLen = in.getUnsignedInt(in.readerIndex() + BYTE_STREAM_LENGTH_FIELD_OFFSET);
        if (in.readableBytes() < (bodyLen + BYTE_STREAM_LENGTH_FIELD_OFFSET + BYTE_STREAM_LENGTH_FIELD_LENGTH))
        {
            return null;
        }

        D_ByteData data = new D_ByteData();
        data.fill(in);

        return data;
    }

    private int bodyLength(int bodyAttr)
    {
        return bodyAttr & MSG_BODY_LENGTH_BITS;
    }

    private Object decodeT808Msg(ByteBuf msgBuf)
    {
        log.info("hex msg: [{}]", ByteBufUtil.hexDump(msgBuf));
        byte[] bytes = MessageUtil.transfer7D27E(BufferUtil.readBytes(msgBuf));
        T808Message message = new T808Message();
        message.fill(bytes);

        int validCode = MessageUtil.validCode(message.validArray());
        if (message.getValid() != validCode)
        {
            log.warn("valid code error!");
            return null;
        }
        return message;
    }
}

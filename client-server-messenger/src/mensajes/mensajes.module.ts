import { Module } from '@nestjs/common';
import { MensajesService } from './mensajes.service.js';

@Module({
  providers: [MensajesService]
})
export class MensajesModule {}

import { Module } from '@nestjs/common';
import { LogEventosService } from './log-eventos.service.js';

@Module({
  providers: [LogEventosService]
})
export class LogEventosModule {}

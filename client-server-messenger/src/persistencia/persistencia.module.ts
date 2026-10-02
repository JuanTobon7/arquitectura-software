import { Module } from '@nestjs/common';
import { PersistenciaService } from './persistencia.service.js';

@Module({
  providers: [PersistenciaService]
})
export class PersistenciaModule {}
